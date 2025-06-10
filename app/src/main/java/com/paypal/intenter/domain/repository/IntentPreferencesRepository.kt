package com.paypal.intenter.domain.repository

import com.paypal.intenter.presentation.state.UriOperationUiModel

interface IntentPreferencesRepository {
    suspend fun saveUrl(url: String)
    suspend fun saveAction(action: String)
    suspend fun savePackageName(packageName: String)
    suspend fun saveBrowserExtraAppId(browserExtraAppId: String)
    suspend fun saveAutoRun(autoRun: Boolean)
    suspend fun saveOperations(operations: List<UriOperationUiModel>)
    suspend fun saveRecentUrls(urls: List<String>)

    suspend fun getUrl(): String
    suspend fun getAction(): String
    suspend fun getPackageName(): String
    suspend fun getBrowserExtraAppId(): String
    suspend fun getAutoRun(): Boolean
    suspend fun getOperations(): List<UriOperationUiModel>
    suspend fun getRecentUrls(): List<String>

    suspend fun loadAllPreferences(): IntentPreferences
}

data class IntentPreferences(
    val url: String = "",
    val action: String = "android.intent.action.VIEW",
    val packageName: String = "com.paypal.android.p2pmobile",
    val browserExtraAppId: String = "",
    val autoRun: Boolean = false,
    val operations: List<UriOperationUiModel> = emptyList(),
    val recentUrls: List<String> = emptyList()
)
