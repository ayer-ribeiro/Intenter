package com.paypal.intenter.presentation.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IntentUiStateTest {

    @Test
    fun `GIVEN default values WHEN creating IntentUiState THEN returns correct defaults`() {
        // GIVEN & WHEN
        val uiState = IntentUiState()

        // THEN
        assertFalse(uiState.isLoading)
        assertEquals(null, uiState.error)
        assertEquals("", uiState.url)
        assertEquals("android.intent.action.VIEW", uiState.action)
        assertEquals("com.paypal.android.p2pmobile", uiState.packageName)
        assertEquals("", uiState.browserExtraAppId)
        assertTrue(uiState.operations.isEmpty())
        assertFalse(uiState.autoRun)
        assertTrue(uiState.flags.isEmpty())
        assertEquals("", uiState.resultingUri)
        assertFalse(uiState.isAutoRunning)
        assertEquals(0, uiState.autoRunCountdown)
    }

    @Test
    fun `GIVEN IntentUiState with copy WHEN modifying single field THEN returns new instance with change`() {
        // GIVEN
        val originalState = IntentUiState(
            url = "https://original.com",
            isLoading = false,
            error = null
        )

        // WHEN
        val modifiedState = originalState.copy(
            url = "https://modified.com",
            isLoading = true
        )

        // THEN
        assertEquals("https://modified.com", modifiedState.url)
        assertTrue(modifiedState.isLoading)
        assertEquals(null, modifiedState.error)
        assertEquals("https://original.com", originalState.url) // Original unchanged
        assertFalse(originalState.isLoading) // Original unchanged
    }
}

class UriOperationUiModelTest {

    @Test
    fun `GIVEN operation parameters WHEN creating UriOperationUiModel THEN returns correct values`() {
        // GIVEN
        val testId = "test-id"
        val testType = "add"
        val testValue = "param=value"
        val testIsEnabled = true

        // WHEN
        val operation = UriOperationUiModel(
            id = testId,
            type = testType,
            value = testValue,
            isEnabled = testIsEnabled
        )

        // THEN
        assertEquals(testId, operation.id)
        assertEquals(testType, operation.type)
        assertEquals(testValue, operation.value)
        assertEquals(testIsEnabled, operation.isEnabled)
    }

    @Test
    fun `GIVEN UriOperationUiModel with copy WHEN modifying fields THEN returns new instance with changes`() {
        // GIVEN
        val originalOperation = UriOperationUiModel("1", "add", "original=value", true)

        // WHEN
        val modifiedOperation = originalOperation.copy(
            type = "replace",
            value = "modified=value",
            isEnabled = false
        )

        // THEN
        assertEquals("1", modifiedOperation.id) // Unchanged
        assertEquals("replace", modifiedOperation.type)
        assertEquals("modified=value", modifiedOperation.value)
        assertFalse(modifiedOperation.isEnabled)
        assertEquals("add", originalOperation.type) // Original unchanged
        assertEquals("original=value", originalOperation.value) // Original unchanged
        assertTrue(originalOperation.isEnabled) // Original unchanged
    }
}

class IntentFlagUiModelTest {

    @Test
    fun `GIVEN flag parameters WHEN creating IntentFlagUiModel THEN returns correct values`() {
        // GIVEN
        val testName = "FLAG_ACTIVITY_NEW_TASK"
        val testValue = 123456
        val testIsSelected = true

        // WHEN
        val flag = IntentFlagUiModel(
            name = testName,
            value = testValue,
            isSelected = testIsSelected
        )

        // THEN
        assertEquals(testName, flag.name)
        assertEquals(testValue, flag.value)
        assertEquals(testIsSelected, flag.isSelected)
    }

    @Test
    fun `GIVEN default isSelected WHEN creating IntentFlagUiModel THEN isSelected is false`() {
        // GIVEN
        val testName = "FLAG_ACTIVITY_NEW_TASK"
        val testValue = 123456

        // WHEN
        val flag = IntentFlagUiModel(
            name = testName,
            value = testValue
        )

        // THEN
        assertEquals(testName, flag.name)
        assertEquals(testValue, flag.value)
        assertFalse(flag.isSelected)
    }

    @Test
    fun `GIVEN IntentFlagUiModel with copy WHEN modifying isSelected THEN returns new instance with change`() {
        // GIVEN
        val originalFlag = IntentFlagUiModel("FLAG_TEST", 123, false)

        // WHEN
        val modifiedFlag = originalFlag.copy(isSelected = true)

        // THEN
        assertEquals("FLAG_TEST", modifiedFlag.name) // Unchanged
        assertEquals(123, modifiedFlag.value) // Unchanged
        assertTrue(modifiedFlag.isSelected)
        assertFalse(originalFlag.isSelected) // Original unchanged
    }
}
