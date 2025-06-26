package com.example.aitoolpoc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
 * Data class to hold login form state
 */
data class LoginState(
    val invitationCode: List<String> = listOf("", "", "", ""),
    val pin: List<String> = listOf("", "", "", ""),
    val isInvitationCodeComplete: Boolean = false,
    val isPinComplete: Boolean = false,
    val isFormValid: Boolean = false
) {
    companion object {
        fun create() = LoginState()
    }

    fun updateInvitationCode(newCode: List<String>): LoginState {
        val isComplete = newCode.size == 4 && newCode.all { it.isNotEmpty() }
        return copy(
            invitationCode = newCode,
            isInvitationCodeComplete = isComplete,
            isFormValid = isComplete && isPinComplete
        )
    }

    fun updatePin(newPin: List<String>): LoginState {
        val isComplete = newPin.size == 4 && newPin.all { it.isNotEmpty() }
        return copy(
            pin = newPin,
            isPinComplete = isComplete,
            isFormValid = isInvitationCodeComplete && isComplete
        )
    }
}

/**
 * Main login screen component based on Figma design
 * Screen dimensions: 375x812 (iPhone design)
 */
@Composable
fun LoginScreen(
    onBackPressed: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    onForgotPinPressed: () -> Unit = {},
    onEmailLoginPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel { LoginViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()
    var loginState by remember { mutableStateOf(LoginState.create()) }

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

            // Spacing to form section - 270dp from top
            Spacer(modifier = Modifier.height(176.dp))

            // Form sections with 41dp gap between them
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(41.dp)
            ) {
                // Invitation Code Section (Alphanumeric)
                InputSection(
                    label = "Enter 4 Character Invitation Code",
                    values = loginState.invitationCode,
                    onValuesChange = { newCode ->
                        loginState = loginState.updateInvitationCode(newCode)
                    },
                    inputType = InputType.ALPHANUMERIC
                )

                // PIN Section with helper text and forgot PIN link
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InputSection(
                        label = "Enter Pin",
                        values = loginState.pin,
                        onValuesChange = { newPin ->
                            loginState = loginState.updatePin(newPin)
                        },
                        inputType = InputType.NUMERIC
                    )

                    // Helper text and Forgot PIN link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enter the PIN you have set while sign up.",
                            style = LoginTypography.HelperText,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "Forgot PIN?",
                            style = LoginTypography.ForgotPinLink,
                            modifier = Modifier.clickable { onForgotPinPressed() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(41.dp))

            // Or divider and Email login section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                OrDivider()

                EmailLoginButton(onClick = onEmailLoginPressed)
            }
        }

        // Bottom Next button - positioned at y: 762dp from top
        NextButton(
            onClick = {
                if (loginState.isFormValid && !uiState.isLoading) {
                    val invitationCodeString = loginState.invitationCode.joinToString("")
                    val pinString = loginState.pin.joinToString("")
                    viewModel.loginWithCode(invitationCodeString, pinString)
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
                // Show error toast or snackbar
                android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}
