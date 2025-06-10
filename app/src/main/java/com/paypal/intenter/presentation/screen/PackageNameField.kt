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
fun PackageNameField(
    packageName: String,
    onPackageNameChanged: (String) -> Unit
) {
    var packageDropdownExpanded by remember { mutableStateOf(false) }
    val packageOptions = listOf(
        "com.android.chrome",
        "com.paypal.android.p2pmobile"
    )

    Column {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = packageName,
            onValueChange = onPackageNameChanged,
            label = { Text("Target app package name") },
            trailingIcon = {
                Row {
                    if (packageName.isNotEmpty()) {
                        IconButton(
                            onClick = { onPackageNameChanged("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear package name",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = if (packageDropdownExpanded) "▲" else "▼",
                        modifier = Modifier.clickable { packageDropdownExpanded = !packageDropdownExpanded }
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        DropdownMenu(
            expanded = packageDropdownExpanded,
            onDismissRequest = { packageDropdownExpanded = false }
        ) {
            packageOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onPackageNameChanged(option)
                        packageDropdownExpanded = false
                    }
                )
            }
        }
    }
}
