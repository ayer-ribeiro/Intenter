package com.paypal.intenter.presentation.action

import com.paypal.intenter.presentation.state.UriOperationUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IntentUiActionTest {

    @Test
    fun `GIVEN url string WHEN creating UpdateUrl action THEN contains correct url`() {
        // GIVEN
        val testUrl = "https://example.com"

        // WHEN
        val action = IntentUiAction.UpdateUrl(testUrl)

        // THEN
        assertEquals(testUrl, action.url)
        assertTrue(action is IntentUiAction.UpdateUrl)
    }

    @Test
    fun `GIVEN action string WHEN creating UpdateAction action THEN contains correct action`() {
        // GIVEN
        val testAction = "android.intent.action.SEND"

        // WHEN
        val action = IntentUiAction.UpdateAction(testAction)

        // THEN
        assertEquals(testAction, action.action)
        assertTrue(action is IntentUiAction.UpdateAction)
    }

    @Test
    fun `GIVEN auto run boolean WHEN creating UpdateAutoRun action THEN contains correct value`() {
        // GIVEN
        val testAutoRun = true

        // WHEN
        val action = IntentUiAction.UpdateAutoRun(testAutoRun)

        // THEN
        assertEquals(testAutoRun, action.autoRun)
        assertTrue(action is IntentUiAction.UpdateAutoRun)
    }

    @Test
    fun `GIVEN operation WHEN creating AddOperation action THEN contains correct operation`() {
        // GIVEN
        val testOperation = UriOperationUiModel("1", "add", "param=value", true)

        // WHEN
        val action = IntentUiAction.AddOperation(testOperation)

        // THEN
        assertEquals(testOperation, action.operation)
        assertTrue(action is IntentUiAction.AddOperation)
    }

    @Test
    fun `GIVEN operation id WHEN creating RemoveOperation action THEN contains correct id`() {
        // GIVEN
        val testOperationId = "operation-123"

        // WHEN
        val action = IntentUiAction.RemoveOperation(testOperationId)

        // THEN
        assertEquals(testOperationId, action.operationId)
        assertTrue(action is IntentUiAction.RemoveOperation)
    }

    @Test
    fun `GIVEN flag name WHEN creating ToggleFlag action THEN contains correct flag name`() {
        // GIVEN
        val testFlagName = "FLAG_ACTIVITY_NEW_TASK"

        // WHEN
        val action = IntentUiAction.ToggleFlag(testFlagName)

        // THEN
        assertEquals(testFlagName, action.flagName)
        assertTrue(action is IntentUiAction.ToggleFlag)
    }

    @Test
    fun `WHEN creating ExecuteIntent action THEN is correct object type`() {
        // GIVEN & WHEN
        val action = IntentUiAction.ExecuteIntent

        // THEN
        assertTrue(action is IntentUiAction.ExecuteIntent)
        assertEquals(IntentUiAction.ExecuteIntent, action)
    }

    @Test
    fun `WHEN creating LoadSavedData action THEN is correct object type`() {
        // GIVEN & WHEN
        val action = IntentUiAction.LoadSavedData

        // THEN
        assertTrue(action is IntentUiAction.LoadSavedData)
        assertEquals(IntentUiAction.LoadSavedData, action)
    }

    @Test
    fun `WHEN creating OpenInCustomTab action THEN is correct object type`() {
        // GIVEN & WHEN
        val action = IntentUiAction.OpenInCustomTab

        // THEN
        assertTrue(action is IntentUiAction.OpenInCustomTab)
        assertEquals(IntentUiAction.OpenInCustomTab, action)
    }
}
