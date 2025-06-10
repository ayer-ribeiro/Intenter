package com.paypal.intenter.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.paypal.intenter.presentation.action.IntentUiAction
import com.paypal.intenter.presentation.state.IntentUiState
import com.paypal.intenter.presentation.state.UriOperationUiModel
import java.util.UUID

@Composable
fun IntentScreen(
    uiState: IntentUiState,
    onAction: (IntentUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Title
            item {
                Text(
                    text = "Intent Trigger / Interceptor",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Loading indicator
            if (uiState.isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Error message
            uiState.error?.let { error ->
                item {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Success banner when triggered from intenter
            if (uiState.triggeredFromIntenter && uiState.triggeredUrl != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Triggered",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Universal / Deeplink Captured",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = uiState.triggeredUrl,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { onAction(IntentUiAction.DismissTriggerBanner) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Dismiss captured intent",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Auto-run countdown
            if (uiState.isAutoRunning) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Auto-running intent in ${uiState.autoRunCountdown} seconds...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { onAction(IntentUiAction.CancelAutoRun) }) { Text("Cancel") }
                        }
                    }
                }
            }

            // Basic input fields
            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.action,
                    onValueChange = { onAction(IntentUiAction.UpdateAction(it)) },
                    label = { Text("Action") }
                )
            }

            item {
                UriField(
                    url = uiState.url,
                    recentUrls = uiState.recentUrls,
                    onUrlChanged = { onAction(IntentUiAction.UpdateUrl(it)) },
                    onSelectRecent = { onAction(IntentUiAction.SelectRecentUrl(it)) }
                )
            }

            item {
                PackageNameField(
                    packageName = uiState.packageName,
                    onPackageNameChanged = { onAction(IntentUiAction.UpdatePackageName(it)) }
                )
            }

            item {
                BrowserExtraAppIdField(
                    browserExtraAppId = uiState.browserExtraAppId,
                    onBrowserExtraAppIdChanged = {
                        onAction(
                            IntentUiAction.UpdateBrowserExtraAppId(
                                it
                            )
                        )
                    }
                )
            }

            // Auto-run checkbox
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = uiState.autoRun,
                        onCheckedChange = { onAction(IntentUiAction.UpdateAutoRun(it)) }
                    )
                    Text("Auto-run intent when receiving external URL")
                }
            }

            // Intent flags section
            item {
                IntentFlagsDropdown(
                    flags = uiState.flags,
                    onFlagToggle = { flagName -> onAction(IntentUiAction.ToggleFlag(flagName)) }
                )
            }

            // URI Operations section
            item {
                Text(
                    text = "URI Operations",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            itemsIndexed(uiState.operations) { _, operation ->
                UriOperationItem(
                    operation = operation,
                    onOperationChanged = { onAction(IntentUiAction.UpdateOperation(it)) },
                    onRemoveOperation = { onAction(IntentUiAction.RemoveOperation(operation.id)) }
                )
            }

            item {
                FloatingActionButton(
                    onClick = {
                        val newOperation = UriOperationUiModel(
                            id = UUID.randomUUID().toString(),
                            type = "add",
                            value = "",
                            isEnabled = true
                        )
                        onAction(IntentUiAction.AddOperation(newOperation))
                    },
                    modifier = Modifier.size(48.dp)
                ) { Icon(Icons.Filled.Add, contentDescription = "Add Operation") }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.resultingUri.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resulting URI",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = uiState.resultingUri,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onAction(IntentUiAction.ExecuteIntent) },
                    modifier = Modifier.weight(1f)
                ) { Text("Execute") }
                Button(
                    onClick = { onAction(IntentUiAction.OpenInCustomTab) },
                    enabled = uiState.url.isNotBlank() || uiState.resultingUri.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) { Text("Custom Tab") }
            }
        }
    }
}

@Composable
private fun UriField(
    url: String,
    recentUrls: List<String>,
    onUrlChanged: (String) -> Unit,
    onSelectRecent: (String) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var textFieldWidth by remember { mutableStateOf(0.dp) }
    Column {
        val density = LocalDensity.current
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldWidth = with(density) { coordinates.size.width.toDp() }
                },
            value = url,
            onValueChange = onUrlChanged,
            label = { Text("Uri") },
            singleLine = false,
            trailingIcon = {
                Row {
                    if (url.isNotEmpty()) {
                        IconButton(
                            onClick = { onUrlChanged("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear uri",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (recentUrls.isNotEmpty()) {
                        Text(
                            text = if (dropdownExpanded) "▲" else "▼",
                            modifier = Modifier
                                .clickable { dropdownExpanded = !dropdownExpanded }
                                .padding(start = 4.dp)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
            modifier = if (textFieldWidth > 0.dp) Modifier.width(textFieldWidth) else Modifier
        ) {
            recentUrls.forEach { recent ->
                DropdownMenuItem(
                    text = { Text(recent) },
                    onClick = {
                        onSelectRecent(recent)
                        dropdownExpanded = false
                    }
                )
            }
        }
    }
}
