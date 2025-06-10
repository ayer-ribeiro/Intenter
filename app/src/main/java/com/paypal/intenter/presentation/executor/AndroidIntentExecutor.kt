package com.paypal.intenter.presentation.executor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.paypal.intenter.presentation.viewmodel.IntentExecutor

class AndroidIntentExecutor(
    private val activity: Activity
) : IntentExecutor {

    override fun executeIntent(
        url: String,
        action: String,
        packageName: String,
        browserExtraAppId: String,
        flags: List<Int>
    ) {
        try {
            val intent = Intent(action).apply {
                data = Uri.parse(url)

                // Set package name if provided
                if (packageName.isNotEmpty()) {
                    setPackage(packageName)
                }

                // Add browser extra app ID if provided
                if (browserExtraAppId.isNotEmpty()) {
                    putExtra("com.android.browser.application_id", browserExtraAppId)
                }

                // Apply all selected flags
                flags.forEach { flag ->
                    addFlags(flag)
                }
            }

            activity.startActivity(intent)

            Toast.makeText(
                activity,
                "Intent executed successfully",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            Toast.makeText(
                activity,
                "Failed to execute intent: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun openInCustomTab(
        url: String,
        action: String,
        packageName: String,
        browserExtraAppId: String,
        flags: List<Int>
    ) {
        try {
            if (url.isBlank()) {
                Toast.makeText(activity, "URL is empty", Toast.LENGTH_SHORT).show()
                return
            }
            val uri = url.toUri()
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()

            // Apply provided parameters to underlying intent
            customTabsIntent.intent.apply {
                // Use provided action if custom tabs can respect it (fallback to VIEW otherwise)
                this.action = action.ifBlank { Intent.ACTION_VIEW }
                if (packageName.isNotBlank()) {
                    `package` = packageName
                }
                if (browserExtraAppId.isNotBlank()) {
                    putExtra("com.android.browser.application_id", browserExtraAppId)
                }
                for (flag in flags) {
                    addFlags(flag)
                }
            }

            customTabsIntent.launchUrl(activity, uri)
            Toast.makeText(activity, "Opened in Custom Tab", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(activity, "Failed to open Custom Tab: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
