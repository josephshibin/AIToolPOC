package com.example.aitoolpoc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aitoolpoc.ui.components.*
import com.example.aitoolpoc.ui.theme.LoginColors
import com.example.aitoolpoc.ui.theme.LoginTypography
import com.example.aitoolpoc.viewmodel.LoginViewModel

/**
 * Data class to hold email login form state
 */
data class EmailLoginState(
    val email: String = "",
    val pin: List<String> = listOf("", "", "", ""),
    val isEmailValid: Boolean = false,
    val isPinComplete: Boolean = false,
    val isFormValid: Boolean = false
) {
    companion object {
        fun create() = EmailLoginState()
    }
    
    fun updateEmail(newEmail: String): EmailLoginState {
        val isValid = newEmail.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()
        return copy(
            email = newEmail,
            isEmailValid = isValid,
            isFormValid = isValid && isPinComplete
        )
    }
    
    fun updatePin(newPin: List<String>): EmailLoginState {
        val isComplete = newPin.size == 4 && newPin.all { it.isNotEmpty() }
        return copy(
            pin = newPin,
            isPinComplete = isComplete,
            isFormValid = isEmailValid && isComplete
        )
    }
}

/**
 * Email login screen component based on Figma design
 * Screen dimensions: 375x812 (iPhone design)
 */
@Composable
fun EmailLoginScreen(
    onBackPressed: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    onForgotPinPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel { LoginViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()
    var loginState by remember { mutableStateOf(EmailLoginState.create()) }

    // Handle login success
    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoginColors.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Top spacing - 38dp from top
            Spacer(modifier = Modifier.height(38.dp))

            // Back arrow - positioned at x: 20.69dp from left edge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(4.69.dp)) // 20.69 - 16 (padding) = 4.69
                BackArrowButton(onClick = onBackPressed)
            }

            // Spacing to title - 70dp from top minus back arrow section
            Spacer(modifier = Modifier.height(16.dp))

            // Login title
            Text(
                text = "Login",
                style = LoginTypography.LoginTitle,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Spacing to form section - 326dp from top (different from PIN login)
            Spacer(modifier = Modifier.height(232.dp))

            // Form sections with 41dp gap between them
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(41.dp)
            ) {
                // Email Input Section
                // Show as text when email is valid and PIN is complete
                EmailInputSection(
                    label = "Enter your registered Email ID",
                    value = loginState.email,
                    onValueChange = { newEmail ->
                        loginState = loginState.updateEmail(newEmail)
                    },
                    placeholder = "example@email.com",
                    showAsText = loginState.isEmailValid && loginState.isPinComplete,
                    onEditEmail = {
                        // Reset PIN when editing email to go back to input mode
                        loginState = loginState.copy(pin = listOf("", "", "", ""))
                    }
                )

                // PIN Section with helper text and forgot PIN link
                // Show as dots when both email and PIN are complete
                PinSectionWithHelper(
                    pinValues = loginState.pin,
                    onPinValuesChange = { newPin ->
                        loginState = loginState.updatePin(newPin)
                    },
                    onForgotPinPressed = onForgotPinPressed,
                    showAsDots = loginState.isEmailValid && loginState.isPinComplete,
                    onEditPin = {
                        // Reset PIN to allow editing
                        loginState = loginState.copy(pin = listOf("", "", "", ""))
                    }
                )
            }
        }

        // Bottom Next button - positioned at y: 762dp from top
        NextButton(
            onClick = {
                if (loginState.isFormValid && !uiState.isLoading) {
                    val pinString = loginState.pin.joinToString("")
                    viewModel.loginWithEmail(loginState.email, pinString)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LoginColors.DarkText.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = LoginColors.TealAccent)
            }
        }

        // Error message
        uiState.errorMessage?.let { error ->
            LaunchedEffect(error) {
                // Show error toast
                android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812)
@Composable
fun EmailLoginScreenPreview() {
    EmailLoginScreen()
}
