package com.paypal.intenter.presentation.viewmodel

import com.paypal.intenter.domain.repository.IntentPreferences
import com.paypal.intenter.domain.repository.IntentPreferencesRepository
import com.paypal.intenter.presentation.action.IntentUiAction
import com.paypal.intenter.presentation.state.UriOperationUiModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IntentViewModelTest {

    private lateinit var preferencesRepository: IntentPreferencesRepository
    private lateinit var intentExecutor: IntentExecutor
    private lateinit var viewModel: IntentViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        preferencesRepository = mockk()
        intentExecutor = mockk(relaxed = true)
        coEvery { preferencesRepository.saveRecentUrls(any()) } returns Unit
        viewModel = IntentViewModel(preferencesRepository, intentExecutor)
    }

    @Test
    fun `GIVEN initial state WHEN viewModel created THEN initializes with default values`() {
        // GIVEN
        // Initial setup in @Before

        // WHEN
        val uiState = viewModel.uiState.value

        // THEN
        assertEquals("", uiState.url)
        assertEquals("android.intent.action.VIEW", uiState.action)
        assertEquals("com.paypal.android.p2pmobile", uiState.packageName)
        assertEquals("", uiState.browserExtraAppId)
        assertFalse(uiState.autoRun)
        assertTrue(uiState.operations.isEmpty())
        assertEquals(19, uiState.flags.size) // Available flags count
        assertTrue(uiState.recentUrls.isEmpty())
        assertFalse(uiState.isLoading)
        assertEquals(null, uiState.error)
    }

    @Test
    fun `GIVEN valid url WHEN UpdateUrl action THEN updates url saves and adds to history`() = runTest {
        // GIVEN
        val testUrl = "https://example.com"
        coEvery { preferencesRepository.saveUrl(any()) } returns Unit

        // WHEN
        viewModel.handleAction(IntentUiAction.UpdateUrl(testUrl))

        // THEN
        assertEquals(testUrl, viewModel.uiState.value.url)
        assertEquals(listOf(testUrl), viewModel.uiState.value.recentUrls)
        coVerify { preferencesRepository.saveUrl(testUrl) }
        coVerify { preferencesRepository.saveRecentUrls(listOf(testUrl)) }
    }

    @Test
    fun `GIVEN existing history WHEN updating with duplicate url THEN history not duplicated and moves to front`() = runTest {
        // GIVEN
        val urls = listOf("https://a.com", "https://b.com", "https://c.com")
        coEvery { preferencesRepository.saveUrl(any()) } returns Unit
        urls.forEach { viewModel.handleAction(IntentUiAction.UpdateUrl(it)) }

        // WHEN
        // Re-add middle url
        viewModel.handleAction(IntentUiAction.UpdateUrl("https://b.com"))

        // THEN
        val history = viewModel.uiState.value.recentUrls
        assertEquals("https://b.com", history.first())
        assertEquals(3, history.size)
    }

    @Test
    fun `GIVEN history over max WHEN adding urls THEN keeps only max items`() = runTest {
        // GIVEN
        coEvery { preferencesRepository.saveUrl(any()) } returns Unit

        // WHEN
        (1..15).forEach { i -> viewModel.handleAction(IntentUiAction.UpdateUrl("https://$i.com")) }

        // THEN
        val history = viewModel.uiState.value.recentUrls
        assertEquals(10, history.size)
        // Most recent first
        assertEquals("https://15.com", history.first())
    }

    @Test
    fun `GIVEN selected recent url WHEN SelectRecentUrl THEN sets url and updates history order`() = runTest {
        // GIVEN
        coEvery { preferencesRepository.saveUrl(any()) } returns Unit
        listOf("https://one.com", "https://two.com", "https://three.com").forEach { viewModel.handleAction(IntentUiAction.UpdateUrl(it)) }

        // WHEN
        viewModel.handleAction(IntentUiAction.SelectRecentUrl("https://one.com"))

        // THEN
        val state = viewModel.uiState.value
        assertEquals("https://one.com", state.url)
        assertEquals("https://one.com", state.recentUrls.first())
    }

    @Test
    fun `GIVEN valid action WHEN UpdateAction action THEN updates action and saves to repository`() = runTest {
        // GIVEN
        val testAction = "android.intent.action.SEND"
        coEvery { preferencesRepository.saveAction(any()) } returns Unit

        // WHEN
        viewModel.handleAction(IntentUiAction.UpdateAction(testAction))

        // THEN
        assertEquals(testAction, viewModel.uiState.value.action)
        coVerify { preferencesRepository.saveAction(testAction) }
    }

    @Test
    fun `GIVEN auto run value WHEN UpdateAutoRun action THEN updates auto run and saves to repository`() = runTest {
        // GIVEN
        val testAutoRun = true
        coEvery { preferencesRepository.saveAutoRun(any()) } returns Unit

        // WHEN
        viewModel.handleAction(IntentUiAction.UpdateAutoRun(testAutoRun))

        // THEN
        assertEquals(testAutoRun, viewModel.uiState.value.autoRun)
        coVerify { preferencesRepository.saveAutoRun(testAutoRun) }
    }

    @Test
    fun `GIVEN new operation WHEN AddOperation action THEN adds operation to list`() = runTest {
        // GIVEN
        val newOperation = UriOperationUiModel("1", "add", "test=value", true)
        coEvery { preferencesRepository.saveOperations(any()) } returns Unit

        // WHEN
        viewModel.handleAction(IntentUiAction.AddOperation(newOperation))

        // THEN
        assertEquals(1, viewModel.uiState.value.operations.size)
        assertEquals(newOperation, viewModel.uiState.value.operations.first())
        coVerify { preferencesRepository.saveOperations(listOf(newOperation)) }
    }

    @Test
    fun `GIVEN flag name WHEN ToggleFlag action THEN toggles flag selection`() {
        // GIVEN
        val flagName = "FLAG_ACTIVITY_NEW_TASK"
        val initialFlag = viewModel.uiState.value.flags.first { it.name == flagName }
        val initialSelection = initialFlag.isSelected

        // WHEN
        viewModel.handleAction(IntentUiAction.ToggleFlag(flagName))

        // THEN
        val updatedFlag = viewModel.uiState.value.flags.first { it.name == flagName }
        assertEquals(!initialSelection, updatedFlag.isSelected)
    }

    @Test
    fun `GIVEN repository data WHEN LoadSavedData action THEN loads and updates state`() = runTest {
        // GIVEN
        val savedPreferences = IntentPreferences(
            url = "https://saved.com",
            action = "android.intent.action.SEND",
            packageName = "com.saved.app",
            browserExtraAppId = "com.saved.browser",
            autoRun = true,
            operations = listOf(UriOperationUiModel("1", "add", "param=saved", true))
        )
        coEvery { preferencesRepository.loadAllPreferences() } returns savedPreferences

        // WHEN
        viewModel.handleAction(IntentUiAction.LoadSavedData)

        // THEN
        val uiState = viewModel.uiState.value
        assertEquals(savedPreferences.url, uiState.url)
        assertEquals(savedPreferences.action, uiState.action)
        assertEquals(savedPreferences.packageName, uiState.packageName)
        assertEquals(savedPreferences.browserExtraAppId, uiState.browserExtraAppId)
        assertEquals(savedPreferences.autoRun, uiState.autoRun)
        assertEquals(savedPreferences.operations, uiState.operations)
        assertFalse(uiState.isLoading)
    }

    @Test
    fun `GIVEN repository error WHEN LoadSavedData action THEN sets error state`() = runTest {
        // GIVEN
        val errorMessage = "Failed to load data"
        coEvery { preferencesRepository.loadAllPreferences() } throws Exception(errorMessage)

        // WHEN
        viewModel.handleAction(IntentUiAction.LoadSavedData)

        // THEN
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("Failed to load saved data: $errorMessage", uiState.error)
    }

    @Test
    fun `GIVEN incoming intent url WHEN HandleIncomingIntent action THEN updates url without saving`() = runTest {
        // GIVEN
        val intentUrl = "https://incoming.com"
        coEvery { preferencesRepository.saveUrl(any()) } returns Unit

        // WHEN
        viewModel.handleAction(IntentUiAction.HandleIncomingIntent(intentUrl))

        // THEN
        assertEquals(intentUrl, viewModel.uiState.value.url)
        coVerify(exactly = 0) { preferencesRepository.saveUrl(any()) }
    }

    @Test
    fun `GIVEN resulting uri WHEN OpenInCustomTab action THEN executor called with resulting uri`() = runTest {
        // GIVEN
        val baseUrl = "https://example.com"
        val operation = UriOperationUiModel("1", "add", "a=1", true)
        coEvery { preferencesRepository.saveUrl(any()) } returns Unit
        coEvery { preferencesRepository.saveOperations(any()) } returns Unit

        viewModel.handleAction(IntentUiAction.UpdateUrl(baseUrl))
        viewModel.handleAction(IntentUiAction.AddOperation(operation))
        val expected = viewModel.uiState.value.resultingUri
        val slotUrl = slot<String>()
        every { intentExecutor.openInCustomTab(capture(slotUrl), any(), any(), any(), any()) } returns Unit

        // WHEN
        viewModel.handleAction(IntentUiAction.OpenInCustomTab)

        // THEN
        verify(exactly = 1) { intentExecutor.openInCustomTab(any(), any(), any(), any(), any()) }
        assertEquals(expected, slotUrl.captured)
    }

    @Test
    fun `GIVEN empty url WHEN OpenInCustomTab action THEN executor not called`() = runTest {
        // GIVEN - initial state has empty url

        // WHEN
        viewModel.handleAction(IntentUiAction.OpenInCustomTab)

        // THEN
        verify(exactly = 0) { intentExecutor.openInCustomTab(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `GIVEN https intenter universal link WHEN HandleIncomingIntent THEN sets trigger fields without changing url`() = runTest {
        // GIVEN
        val incoming = "https://intenter.com/callback?x=1"
        val originalUrl = viewModel.uiState.value.url

        // WHEN
        viewModel.handleAction(IntentUiAction.HandleIncomingIntent(incoming))

        // THEN
        val state = viewModel.uiState.value
        assertTrue(state.triggeredFromIntenter)
        assertEquals(incoming, state.triggeredUrl)
        assertEquals(originalUrl, state.url) // url unchanged
        assertEquals(originalUrl, state.resultingUri) // resultingUri unchanged
    }

    @Test
    fun `GIVEN custom scheme intenter deeplink WHEN HandleIncomingIntent THEN sets trigger fields without changing url`() = runTest {
        // GIVEN
        val incoming = "intenter://anything/path?y=2"
        val originalUrl = viewModel.uiState.value.url

        // WHEN
        viewModel.handleAction(IntentUiAction.HandleIncomingIntent(incoming))

        // THEN
        val state = viewModel.uiState.value
        assertTrue(state.triggeredFromIntenter)
        assertEquals(incoming, state.triggeredUrl)
        assertEquals(originalUrl, state.url)
        assertEquals(originalUrl, state.resultingUri)
    }

    @Test
    fun `GIVEN non-intenter link WHEN HandleIncomingIntent THEN updates url and clears trigger`() = runTest {
        // GIVEN
        val incoming = "https://example.com/path"

        // WHEN
        viewModel.handleAction(IntentUiAction.HandleIncomingIntent(incoming))

        // THEN
        val state = viewModel.uiState.value
        assertFalse(state.triggeredFromIntenter)
        assertEquals(null, state.triggeredUrl)
        assertEquals(incoming, state.url)
        assertEquals(incoming, state.resultingUri)
    }

    @Test
    fun `GIVEN trigger active WHEN UpdateUrl THEN clears trigger fields`() = runTest {
        // GIVEN
        val incoming = "intenter://some"
        viewModel.handleAction(IntentUiAction.HandleIncomingIntent(incoming))
        assertTrue(viewModel.uiState.value.triggeredFromIntenter) // precondition
        coEvery { preferencesRepository.saveUrl(any()) } returns Unit

        // WHEN
        viewModel.handleAction(IntentUiAction.UpdateUrl("https://other.com"))

        // THEN
        val state = viewModel.uiState.value
        assertFalse(state.triggeredFromIntenter)
        assertEquals(null, state.triggeredUrl)
    }

    @Test
    fun `GIVEN trigger banner visible WHEN DismissTriggerBanner THEN clears trigger fields`() = runTest {
        // GIVEN
        val incoming = "intenter://deeplink"
        viewModel.handleAction(IntentUiAction.HandleIncomingIntent(incoming))
        assertTrue(viewModel.uiState.value.triggeredFromIntenter) // precondition

        // WHEN
        viewModel.handleAction(IntentUiAction.DismissTriggerBanner)

        // THEN
        val state = viewModel.uiState.value
        assertFalse(state.triggeredFromIntenter)
        assertNull(state.triggeredUrl)
    }
}
