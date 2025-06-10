package com.paypal.intenter.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BrowserExtraAppIdField(
    browserExtraAppId: String,
    onBrowserExtraAppIdChanged: (String) -> Unit
) {
    var browserExtraAppIdDropdownExpanded by remember { mutableStateOf(false) }
    val browserExtraAppIdOptions = listOf(
        "com.android.chrome",
        "example.any.app.id",
        "com.paypal.android.p2pmobile"
    )

    Column {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = browserExtraAppId,
            onValueChange = onBrowserExtraAppIdChanged,
            label = { Text("Extra: Caller App ID") },
            placeholder = { Text("Optional: e.g., com.android.chrome") },
            trailingIcon = {
                Row {
                    if (browserExtraAppId.isNotEmpty()) {
                        IconButton(
                            onClick = { onBrowserExtraAppIdChanged("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear browser extra app ID",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = if (browserExtraAppIdDropdownExpanded) "▲" else "▼",
                        modifier = Modifier.clickable { browserExtraAppIdDropdownExpanded = !browserExtraAppIdDropdownExpanded }
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        DropdownMenu(
            expanded = browserExtraAppIdDropdownExpanded,
            onDismissRequest = { browserExtraAppIdDropdownExpanded = false }
        ) {
            browserExtraAppIdOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onBrowserExtraAppIdChanged(option)
                        browserExtraAppIdDropdownExpanded = false
                    }
                )
            }
        }
    }
}
