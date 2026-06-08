package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FinanceScreen
import com.example.ui.FinanceViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {
  private var isAuthenticated by mutableStateOf(false)
  private var isAppLockEnabled by mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
    isAppLockEnabled = prefs.getBoolean("biometric_enabled", false)

    if (isAppLockEnabled) {
      showBiometricPrompt()
    } else {
      isAuthenticated = true
    }
    
    setContent {
      if (isAuthenticated) {
        val viewModel: FinanceViewModel = viewModel(factory = FinanceViewModel.Factory(application))
        val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
        
        MyApplicationTheme(themeType = themeMode) {
          FinanceScreen(viewModel)
        }
      } else {
        // Simple Lock Screen layout until authenticated
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Default.Lock,
                    contentDescription = null,
                    modifier = androidx.compose.ui.Modifier.size(64.dp),
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                androidx.compose.material3.Text("Aplikasi Terkunci", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
                androidx.compose.material3.Button(onClick = { showBiometricPrompt() }) {
                    androidx.compose.material3.Text("Buka dengan Sidik Jari")
                }
            }
        }
      }
    }
  }

  private fun showBiometricPrompt() {
    val executor = ContextCompat.getMainExecutor(this)
    val biometricPrompt = BiometricPrompt(this, executor,
      object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
          super.onAuthenticationSucceeded(result)
          isAuthenticated = true
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
          super.onAuthenticationError(errorCode, errString)
          Toast.makeText(applicationContext, "Autentikasi gagal: $errString", Toast.LENGTH_SHORT).show()
        }

        override fun onAuthenticationFailed() {
          super.onAuthenticationFailed()
          Toast.makeText(applicationContext, "Autentikasi gagal", Toast.LENGTH_SHORT).show()
        }
      })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
      .setTitle("Login Sidik Jari")
      .setSubtitle("Masuk menggunakan sidik jari Anda")
      .setNegativeButtonText("Gunakan Password")
      .build()

    biometricPrompt.authenticate(promptInfo)
  }
}


