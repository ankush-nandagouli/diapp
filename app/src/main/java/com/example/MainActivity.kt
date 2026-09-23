package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DakshyamViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: DakshyamViewModel by viewModels {
    DakshyamViewModel.provideFactory(application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

        if (isUserLoggedIn) {
          MainDashboardScreen(
            viewModel = viewModel,
            onLogout = { viewModel.signOutPartner() }
          )
        } else {
          LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = { viewModel.setLoggedIn(true) }
          )
        }
      }
    }
  }
}

