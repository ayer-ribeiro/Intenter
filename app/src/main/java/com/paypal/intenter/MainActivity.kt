package com.paypal.intenter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.paypal.intenter.data.repository.IntentPreferencesRepositoryImpl
import com.paypal.intenter.presentation.action.IntentUiAction
import com.paypal.intenter.presentation.executor.AndroidIntentExecutor
import com.paypal.intenter.presentation.screen.IntentScreen
import com.paypal.intenter.presentation.viewmodel.IntentViewModel
import com.paypal.intenter.ui.theme.IntenterTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: IntentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create ViewModel using ViewModelProvider
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return IntentViewModel(
                    preferencesRepository = IntentPreferencesRepositoryImpl(this@MainActivity),
                    intentExecutor = AndroidIntentExecutor(this@MainActivity)
                ) as T
            }
        }

        viewModel = ViewModelProvider(this, factory)[IntentViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            IntenterTheme {
                val uiState = viewModel.uiState.collectAsState().value

                // Load saved data on first launch
                LaunchedEffect(Unit) {
                    viewModel.handleAction(IntentUiAction.LoadSavedData)
                }

                // Handle incoming intent data
                LaunchedEffect(intent.data) {
                    intent.data?.let { uri ->
                        viewModel.handleAction(IntentUiAction.HandleIncomingIntent(uri.toString()))
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    IntentScreen(
                        uiState = uiState,
                        onAction = viewModel::handleAction,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Handle new intent data with existing ViewModel
        intent.data?.let { uri ->
            viewModel.handleAction(IntentUiAction.HandleIncomingIntent(uri.toString()))
        }
    }
}
