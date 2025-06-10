package com.paypal.intenter.presentation.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paypal.intenter.domain.repository.IntentPreferencesRepository
import com.paypal.intenter.presentation.action.IntentUiAction
import com.paypal.intenter.presentation.state.IntentFlagUiModel
import com.paypal.intenter.presentation.state.UriOperationUiModel
import com.paypal.intenter.presentation.state.IntentUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IntentViewModel(
    private val preferencesRepository: IntentPreferencesRepository,
    private val intentExecutor: IntentExecutor
) : ViewModel() {

    private val _uiState = MutableStateFlow(IntentUiState())
    val uiState: StateFlow<IntentUiState> = _uiState.asStateFlow()

    private var urlFromIntent = false

    private val maxHistory = 10

    // Available intent flags - only include those available in API 24+
    private val availableFlags = listOf(
        IntentFlagUiModel("FLAG_ACTIVITY_NO_HISTORY", Intent.FLAG_ACTIVITY_NO_HISTORY),
        IntentFlagUiModel("FLAG_ACTIVITY_SINGLE_TOP", Intent.FLAG_ACTIVITY_SINGLE_TOP),
        IntentFlagUiModel("FLAG_ACTIVITY_NEW_TASK", Intent.FLAG_ACTIVITY_NEW_TASK),
        IntentFlagUiModel("FLAG_ACTIVITY_MULTIPLE_TASK", Intent.FLAG_ACTIVITY_MULTIPLE_TASK),
        IntentFlagUiModel("FLAG_ACTIVITY_CLEAR_TOP", Intent.FLAG_ACTIVITY_CLEAR_TOP),
        IntentFlagUiModel("FLAG_ACTIVITY_FORWARD_RESULT", Intent.FLAG_ACTIVITY_FORWARD_RESULT),
        IntentFlagUiModel("FLAG_ACTIVITY_PREVIOUS_IS_TOP", Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP),
        IntentFlagUiModel("FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS", Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS),
        IntentFlagUiModel("FLAG_ACTIVITY_BROUGHT_TO_FRONT", Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT),
        IntentFlagUiModel("FLAG_ACTIVITY_RESET_TASK_IF_NEEDED", Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
        IntentFlagUiModel("FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY", Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY),
        IntentFlagUiModel("FLAG_ACTIVITY_NEW_DOCUMENT", Intent.FLAG_ACTIVITY_NEW_DOCUMENT),
        IntentFlagUiModel("FLAG_ACTIVITY_NO_USER_ACTION", Intent.FLAG_ACTIVITY_NO_USER_ACTION),
        IntentFlagUiModel("FLAG_ACTIVITY_REORDER_TO_FRONT", Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
        IntentFlagUiModel("FLAG_ACTIVITY_NO_ANIMATION", Intent.FLAG_ACTIVITY_NO_ANIMATION),
        IntentFlagUiModel("FLAG_ACTIVITY_CLEAR_TASK", Intent.FLAG_ACTIVITY_CLEAR_TASK),
        IntentFlagUiModel("FLAG_ACTIVITY_TASK_ON_HOME", Intent.FLAG_ACTIVITY_TASK_ON_HOME),
        IntentFlagUiModel("FLAG_ACTIVITY_RETAIN_IN_RECENTS", Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS),
        IntentFlagUiModel("FLAG_ACTIVITY_LAUNCH_ADJACENT", Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
    )

    init {
        // Initialize flags
        _uiState.update { it.copy(flags = availableFlags) }
    }

    fun handleAction(action: IntentUiAction) {
        when (action) {
            is IntentUiAction.UpdateUrl -> updateUrl(action.url)
            is IntentUiAction.UpdateAction -> updateAction(action.action)
            is IntentUiAction.UpdatePackageName -> updatePackageName(action.packageName)
            is IntentUiAction.UpdateBrowserExtraAppId -> updateBrowserExtraAppId(action.browserExtraAppId)
            is IntentUiAction.UpdateAutoRun -> updateAutoRun(action.autoRun)
            is IntentUiAction.UpdateOperations -> updateOperations(action.operations)
            is IntentUiAction.UpdateFlags -> updateFlags(action.flags)
            is IntentUiAction.AddOperation -> addOperation(action.operation)
            is IntentUiAction.RemoveOperation -> removeOperation(action.operationId)
            is IntentUiAction.UpdateOperation -> updateOperation(action.operation)
            is IntentUiAction.ToggleFlag -> toggleFlag(action.flagName)
            is IntentUiAction.ExecuteIntent -> executeIntent()
            is IntentUiAction.OpenInCustomTab -> openInCustomTab()
            is IntentUiAction.CancelAutoRun -> cancelAutoRun()
            is IntentUiAction.HandleIncomingIntent -> handleIncomingIntent(action.url)
            is IntentUiAction.LoadSavedData -> loadSavedData()
            is IntentUiAction.SelectRecentUrl -> updateUrl(action.url)
            is IntentUiAction.DismissTriggerBanner -> dismissTriggerBanner()
        }
    }

    private fun updateUrl(url: String) {
        val previous = _uiState.value.url
        _uiState.update {
            it.copy(
                url = url,
                resultingUri = calculateResultingUri(url, it.operations),
                // Reset trigger flags on manual URL change
                triggeredFromIntenter = false,
                triggeredUrl = null
            )
        }

        // Reset the flag when user manually changes URL
        if (urlFromIntent && url != previous) {
            urlFromIntent = false
        }

        // Save to DataStore only if URL didn't come from intent
        if (!urlFromIntent) {
            viewModelScope.launch {
                preferencesRepository.saveUrl(url)
                updateHistory(url)
            }
        }
    }

    private suspend fun updateHistory(newUrl: String) {
        if (newUrl.isBlank()) return
        val current = _uiState.value.recentUrls
        val updated = listOf(newUrl) + current.filter { it != newUrl }
        val limited = updated.take(maxHistory)
        _uiState.update { it.copy(recentUrls = limited) }
        preferencesRepository.saveRecentUrls(limited)
    }

    private fun updateAction(action: String) {
        _uiState.update { it.copy(action = action) }
        viewModelScope.launch {
            preferencesRepository.saveAction(action)
        }
    }

    private fun updatePackageName(packageName: String) {
        _uiState.update { it.copy(packageName = packageName) }
        viewModelScope.launch {
            preferencesRepository.savePackageName(packageName)
        }
    }

    private fun updateBrowserExtraAppId(browserExtraAppId: String) {
        _uiState.update { it.copy(browserExtraAppId = browserExtraAppId) }
        viewModelScope.launch {
            preferencesRepository.saveBrowserExtraAppId(browserExtraAppId)
        }
    }

    private fun updateAutoRun(autoRun: Boolean) {
        _uiState.update { it.copy(autoRun = autoRun) }
        viewModelScope.launch {
            preferencesRepository.saveAutoRun(autoRun)
        }
    }

    private fun updateOperations(operations: List<UriOperationUiModel>) {
        _uiState.update {
            it.copy(
                operations = operations,
                resultingUri = calculateResultingUri(it.url, operations)
            )
        }
        viewModelScope.launch {
            preferencesRepository.saveOperations(operations)
        }
    }

    private fun updateFlags(flags: List<IntentFlagUiModel>) {
        _uiState.update { it.copy(flags = flags) }
    }

    private fun addOperation(operation: UriOperationUiModel) {
        val current = _uiState.value.operations
        updateOperations(current + operation)
    }

    private fun removeOperation(operationId: String) {
        val current = _uiState.value.operations
        updateOperations(current.filterNot { it.id == operationId })
    }

    private fun updateOperation(operation: UriOperationUiModel) {
        val current = _uiState.value.operations
        updateOperations(current.map { if (it.id == operation.id) operation else it })
    }

    private fun toggleFlag(flagName: String) {
        val currentFlags = _uiState.value.flags
        val newFlags = currentFlags.map { if (it.name == flagName) it.copy(isSelected = !it.isSelected) else it }
        updateFlags(newFlags)
    }

    private fun executeIntent() {
        val state = _uiState.value
        val selectedFlags = state.flags.filter { it.isSelected }.map { it.value }
        intentExecutor.executeIntent(
            url = state.resultingUri,
            action = state.action,
            packageName = state.packageName,
            browserExtraAppId = state.browserExtraAppId,
            flags = selectedFlags
        )
    }

    private fun openInCustomTab() {
        val state = _uiState.value
        val urlToOpen = state.resultingUri.ifBlank { state.url }
        if (urlToOpen.isNotBlank()) {
            val selectedFlags = state.flags.filter { it.isSelected }.map { it.value }
            intentExecutor.openInCustomTab(
                url = urlToOpen,
                action = state.action,
                packageName = state.packageName,
                browserExtraAppId = state.browserExtraAppId,
                flags = selectedFlags
            )
        }
    }

    private fun handleIncomingIntent(url: String) {
        val fromIntenter = isIntenterLink(url)
        if (fromIntenter) {
            // Record trigger only; do not overwrite existing URL/resultingUri
            _uiState.update {
                it.copy(
                    triggeredFromIntenter = true,
                    triggeredUrl = url
                )
            }
            // Do NOT set urlFromIntent so manual edits will persist
        } else {
            urlFromIntent = true
            _uiState.update {
                it.copy(
                    url = url,
                    resultingUri = calculateResultingUri(url, it.operations),
                    triggeredFromIntenter = false,
                    triggeredUrl = null
                )
            }
            // Add to history (don't persist original url from intenter deep links)
            viewModelScope.launch { updateHistory(url) }
            if (_uiState.value.autoRun) {
                triggerAutoRun()
            }
        }
    }

    private fun isIntenterLink(url: String): Boolean {
        val lower = url.lowercase()
        if (lower.startsWith("https://intenter.com") || lower.startsWith("intenter://")) return true
        return try {
            val uri = Uri.parse(url)
            (uri.scheme == "https" && uri.host == "intenter.com") || (uri.scheme == "intenter")
        } catch (e: Exception) { false }
    }

    private fun triggerAutoRun() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAutoRunning = true, autoRunCountdown = 3) }
            repeat(3) { countdown ->
                delay(1000)
                _uiState.update { it.copy(autoRunCountdown = 2 - countdown) }
            }
            if (_uiState.value.isAutoRunning) {
                executeIntent()
                _uiState.update { it.copy(isAutoRunning = false, autoRunCountdown = 0) }
            }
        }
    }

    private fun cancelAutoRun() {
        _uiState.update { it.copy(isAutoRunning = false, autoRunCountdown = 0) }
    }

    private fun loadSavedData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val preferences = preferencesRepository.loadAllPreferences()

                // Load URL from DataStore only if no intent data was provided
                val url = if (!urlFromIntent) preferences.url else _uiState.value.url

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        url = url,
                        action = preferences.action,
                        packageName = preferences.packageName,
                        browserExtraAppId = preferences.browserExtraAppId,
                        autoRun = preferences.autoRun,
                        operations = preferences.operations,
                        resultingUri = calculateResultingUri(url, preferences.operations),
                        recentUrls = preferences.recentUrls
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load saved data: ${e.message}") }
            }
        }
    }

    private fun dismissTriggerBanner() {
        _uiState.update { it.copy(triggeredFromIntenter = false, triggeredUrl = null) }
    }

    private fun calculateResultingUri(url: String, operations: List<UriOperationUiModel>): String {
        return operations.filter { it.isEnabled && it.value.isNotEmpty() }
            .fold(url) { acc, operation ->
                when (operation.type) {
                    "add" -> if (acc.contains("?")) "$acc&${operation.value}" else "$acc?${operation.value}"
                    "remove" -> acc.replace(operation.value, "")
                    "replace" -> {
                        val parts = operation.value.split("|", limit = 2)
                        if (parts.size == 2) acc.replace(parts[0], parts[1]) else acc
                    }
                    else -> acc
                }
            }
    }
}

// Interface for intent execution to allow for testing
interface IntentExecutor {
    fun executeIntent(
        url: String,
        action: String,
        packageName: String,
        browserExtraAppId: String,
        flags: List<Int>
    )
    fun openInCustomTab(
        url: String,
        action: String,
        packageName: String,
        browserExtraAppId: String,
        flags: List<Int>
    )
}
