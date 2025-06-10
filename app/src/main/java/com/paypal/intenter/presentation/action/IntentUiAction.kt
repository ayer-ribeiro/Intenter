package com.paypal.intenter.presentation.action

import com.paypal.intenter.presentation.state.UriOperationUiModel
import com.paypal.intenter.presentation.state.IntentFlagUiModel

sealed class IntentUiAction {
    data class UpdateUrl(val url: String) : IntentUiAction()
    data class UpdateAction(val action: String) : IntentUiAction()
    data class UpdatePackageName(val packageName: String) : IntentUiAction()
    data class UpdateBrowserExtraAppId(val browserExtraAppId: String) : IntentUiAction()
    data class UpdateAutoRun(val autoRun: Boolean) : IntentUiAction()
    data class UpdateOperations(val operations: List<UriOperationUiModel>) : IntentUiAction()
    data class UpdateFlags(val flags: List<IntentFlagUiModel>) : IntentUiAction()
    data class AddOperation(val operation: UriOperationUiModel) : IntentUiAction()
    data class RemoveOperation(val operationId: String) : IntentUiAction()
    data class UpdateOperation(val operation: UriOperationUiModel) : IntentUiAction()
    data class ToggleFlag(val flagName: String) : IntentUiAction()
    object ExecuteIntent : IntentUiAction()
    object OpenInCustomTab : IntentUiAction()
    object CancelAutoRun : IntentUiAction()
    data class HandleIncomingIntent(val url: String) : IntentUiAction()
    object LoadSavedData : IntentUiAction()
    data class SelectRecentUrl(val url: String) : IntentUiAction()
    object DismissTriggerBanner : IntentUiAction()
}
