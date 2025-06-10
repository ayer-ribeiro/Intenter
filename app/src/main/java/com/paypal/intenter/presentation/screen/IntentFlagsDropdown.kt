package com.paypal.intenter.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.paypal.intenter.presentation.state.IntentFlagUiModel

@Composable
fun IntentFlagsDropdown(
    flags: List<IntentFlagUiModel>,
    onFlagToggle: (String) -> Unit
) {
    var flagsDropdownExpanded by remember { mutableStateOf(false) }
    val selectedFlags = flags.filter { it.isSelected }
    val availableFlags = flags.filterNot { it.isSelected }

    Column {
        Text(
            text = "Intent Flags",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (selectedFlags.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Selected Flags:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    selectedFlags.forEach { flag ->
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = flag.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            IconButton(
                                onClick = { onFlagToggle(flag.name) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Remove flag",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Text(
                text = "No flags selected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Spacer for margin between selected flags (or no flags text) and the add intent flag field
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))

        if (availableFlags.isNotEmpty()) {
            Box {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { flagsDropdownExpanded = !flagsDropdownExpanded },
                    value = "Add Intent Flag",
                    onValueChange = { },
                    label = { Text("Add Intent Flag") },
                    readOnly = true,
                    trailingIcon = {
                        Text(
                            text = if (flagsDropdownExpanded) "▲" else "▼",
                            modifier = Modifier.clickable { flagsDropdownExpanded = !flagsDropdownExpanded }
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                DropdownMenu(
                    expanded = flagsDropdownExpanded,
                    onDismissRequest = { flagsDropdownExpanded = false }
                ) {
                    availableFlags.forEach { flag ->
                        DropdownMenuItem(
                            text = { Text(flag.name, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                onFlagToggle(flag.name)
                                flagsDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
