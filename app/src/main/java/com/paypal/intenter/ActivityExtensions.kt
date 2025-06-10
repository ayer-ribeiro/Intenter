package com.paypal.intenter

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.paypal.intenter.presentation.state.UriOperationUiModel

/**
 * Extension function for Activity to create and execute an intent with URI operations
 * and browser extra app ID support.
 *
 * @param url The base URL to process
 * @param action The intent action (e.g., "android.intent.action.VIEW")
 * @param packageName The target package name for the intent
 * @param browserExtraAppId Optional browser extra app ID to include in the intent
 * @param operations List of URI operations to apply to the URL
 * @param flags List of intent flags to apply
 */
fun Activity.createAndExecuteIntent(
    url: String,
    action: String,
    packageName: String,
    browserExtraAppId: String,
    operations: List<UriOperationUiModel>,
    flags: List<Int> = emptyList()
) {
    try {
        val finalUrl = operations
            .filter { it.isEnabled && it.value.isNotEmpty() }
            .fold(url) { acc, operation ->
                applyOperationToUrl(acc, operation.type, operation.value)
            }

        val intentUri = if (operations.any { it.isEnabled && it.value.isNotEmpty() }) finalUrl else url
        val intent = Intent(action, intentUri.toUri()).apply {
            if (packageName.isNotBlank()) {
                setPackage(packageName)
            }
            // Add Browser Extra App ID if provided
            if (browserExtraAppId.isNotBlank()) {
                putExtra("com.android.browser.application_id", browserExtraAppId)
            }
            // Add selected flags
            flags.forEach { flag ->
                addFlags(flag)
            }
        }

        startActivity(intent)

        Toast.makeText(
            this,
            "Intent executed successfully",
            Toast.LENGTH_SHORT
        ).show()

    } catch (e: Exception) {
        Toast.makeText(
            this,
            "Failed to execute intent: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}

/**
 * Helper function to apply URL operations
 */
private fun applyOperationToUrl(url: String, operationType: String, operationValue: String): String {
    return when (operationType) {
        "add" -> {
            if (url.contains("?")) {
                "$url&$operationValue"
            } else {
                "$url?$operationValue"
            }
        }
        "remove" -> {
            url.replace(operationValue, "")
        }
        "replace" -> {
            val parts = operationValue.split("|", limit = 2)
            if (parts.size == 2) {
                url.replace(parts[0], parts[1])
            } else {
                url
            }
        }
        else -> url
    }
}
