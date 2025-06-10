package com.paypal.intenter.presentation.state

data class IntentUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val url: String = "",
    val action: String = "android.intent.action.VIEW",
    val packageName: String = "com.paypal.android.p2pmobile",
    val browserExtraAppId: String = "",
    val operations: List<UriOperationUiModel> = emptyList(),
    val autoRun: Boolean = false,
    val flags: List<IntentFlagUiModel> = emptyList(),
    val resultingUri: String = "",
    val isAutoRunning: Boolean = false,
    val autoRunCountdown: Int = 0,
    val triggeredFromIntenter: Boolean = false,
    val triggeredUrl: String? = null,
    val recentUrls: List<String> = emptyList() // Added recent URLs list
)

data class UriOperationUiModel(
    val id: String,
    val type: String,
    val value: String,
    val isEnabled: Boolean
)

data class IntentFlagUiModel(
    val name: String,
    val value: Int,
    val isSelected: Boolean = false
)
