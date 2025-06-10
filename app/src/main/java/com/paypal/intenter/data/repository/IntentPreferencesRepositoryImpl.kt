package com.paypal.intenter.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.paypal.intenter.domain.repository.IntentPreferences
import com.paypal.intenter.domain.repository.IntentPreferencesRepository
import com.paypal.intenter.presentation.state.UriOperationUiModel
import kotlinx.coroutines.flow.first
import java.util.UUID

// DataStore extension
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "intenter_prefs")

// Preference keys
private val URL_KEY = stringPreferencesKey("url")
private val ACTION_KEY = stringPreferencesKey("action")
private val PACKAGE_NAME_KEY = stringPreferencesKey("package_name")
private val BROWSER_EXTRA_APP_ID_KEY = stringPreferencesKey("browser_extra_app_id")
private val OPERATIONS_KEY = stringPreferencesKey("operations")
private val AUTO_RUN_KEY = stringPreferencesKey("auto_run")
// New key for recent urls history (pipe-delimited)
private val RECENT_URLS_KEY = stringPreferencesKey("recent_urls")

class IntentPreferencesRepositoryImpl(
    private val context: Context
) : IntentPreferencesRepository {

    override suspend fun saveUrl(url: String) {
        saveToDataStore(URL_KEY, url)
    }

    override suspend fun saveAction(action: String) {
        saveToDataStore(ACTION_KEY, action)
    }

    override suspend fun savePackageName(packageName: String) {
        saveToDataStore(PACKAGE_NAME_KEY, packageName)
    }

    override suspend fun saveBrowserExtraAppId(browserExtraAppId: String) {
        saveToDataStore(BROWSER_EXTRA_APP_ID_KEY, browserExtraAppId)
    }

    override suspend fun saveAutoRun(autoRun: Boolean) {
        saveToDataStore(AUTO_RUN_KEY, autoRun.toString())
    }

    override suspend fun saveOperations(operations: List<UriOperationUiModel>) {
        context.dataStore.edit { preferences ->
            preferences[OPERATIONS_KEY] = operations.joinToString(";") { "${it.type}:${it.value}" }
        }
    }

    override suspend fun saveRecentUrls(urls: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[RECENT_URLS_KEY] = urls.joinToString("|")
        }
    }

    override suspend fun getUrl(): String {
        return context.dataStore.data.first()[URL_KEY] ?: ""
    }

    override suspend fun getAction(): String {
        return context.dataStore.data.first()[ACTION_KEY] ?: "android.intent.action.VIEW"
    }

    override suspend fun getPackageName(): String {
        return context.dataStore.data.first()[PACKAGE_NAME_KEY] ?: "com.paypal.android.p2pmobile"
    }

    override suspend fun getBrowserExtraAppId(): String {
        return context.dataStore.data.first()[BROWSER_EXTRA_APP_ID_KEY] ?: ""
    }

    override suspend fun getAutoRun(): Boolean {
        return context.dataStore.data.first()[AUTO_RUN_KEY]?.toBoolean() ?: false
    }

    override suspend fun getOperations(): List<UriOperationUiModel> {
        val opsString = context.dataStore.data.first()[OPERATIONS_KEY]
        return opsString?.let {
            if (it.isNotEmpty()) {
                it.split(";").mapNotNull { opString ->
                    val parts = opString.split(":", limit = 2)
                    if (parts.size == 2) {
                        UriOperationUiModel(
                            id = UUID.randomUUID().toString(),
                            type = parts[0],
                            value = parts[1],
                            isEnabled = true
                        )
                    } else {
                        null
                    }
                }
            } else {
                emptyList()
            }
        } ?: emptyList()
    }

    override suspend fun getRecentUrls(): List<String> {
        val stored = context.dataStore.data.first()[RECENT_URLS_KEY] ?: return emptyList()
        if (stored.isBlank()) return emptyList()
        return stored.split("|").filter { it.isNotBlank() }
    }

    override suspend fun loadAllPreferences(): IntentPreferences {
        val preferences = context.dataStore.data.first()

        return IntentPreferences(
            url = preferences[URL_KEY] ?: "",
            action = preferences[ACTION_KEY] ?: "android.intent.action.VIEW",
            packageName = preferences[PACKAGE_NAME_KEY] ?: "com.paypal.android.p2pmobile",
            browserExtraAppId = preferences[BROWSER_EXTRA_APP_ID_KEY] ?: "",
            autoRun = preferences[AUTO_RUN_KEY]?.toBoolean() ?: false,
            operations = getOperations(),
            recentUrls = getRecentUrls()
        )
    }

    private suspend fun saveToDataStore(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }
}
