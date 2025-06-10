package com.paypal.intenter.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.paypal.intenter.presentation.state.UriOperationUiModel

@Composable
fun UriOperationItem(
    operation: UriOperationUiModel,
    onOperationChanged: (UriOperationUiModel) -> Unit,
    onRemoveOperation: () -> Unit
) {
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    val operationTypes = listOf("add", "remove", "replace")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = operation.isEnabled,
                    onCheckedChange = { onOperationChanged(operation.copy(isEnabled = it)) }
                )
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = operation.type,
                        onValueChange = {},
                        label = { Text("Type") },
                        readOnly = true,
                        trailingIcon = {
                            Text(
                                text = if (typeDropdownExpanded) "▲" else "▼",
                                modifier = Modifier.clickable { typeDropdownExpanded = !typeDropdownExpanded }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        operationTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    onOperationChanged(operation.copy(type = type))
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = operation.value,
                        onValueChange = { onOperationChanged(operation.copy(value = it)) },
                        label = { Text("Value") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                IconButton(onClick = onRemoveOperation) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Remove Operation",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}
