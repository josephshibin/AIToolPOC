package com.example.aitoolpoc.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aitoolpoc.network.ApiResult
import com.example.aitoolpoc.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for login operations
 */
class LoginViewModel(context: Context) : ViewModel() {
    
    private val authRepository = AuthRepository(context)
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    /**
     * Login with email and password
     */
    fun loginWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter both email and password"
            )
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter a valid email address"
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )
        
        viewModelScope.launch {
            when (val result = authRepository.loginWithEmail(email, password)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        userData = result.data.user
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is ApiResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    /**
     * Login with signup code and password
     */
    fun loginWithCode(signUpCode: String, password: String) {
        if (signUpCode.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter both signup code and password"
            )
            return
        }
        
        if (signUpCode.length != 4) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Signup code must be 4 characters"
            )
            return
        }

        if (!signUpCode.all { char -> char.isDigit() || char.isLetter() }) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Signup code must contain only letters and numbers"
            )
            return
        }
        
        if (password.length != 4) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "PIN must be 4 digits"
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )
        
        viewModelScope.launch {
            when (val result = authRepository.loginWithCode(signUpCode, password)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        userData = result.data.user
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is ApiResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Reset login state
     */
    fun resetLoginState() {
        _uiState.value = _uiState.value.copy(isLoginSuccessful = false)
    }
    
    /**
     * Check if user is already logged in
     */
    fun checkLoginStatus(): Boolean {
        return authRepository.isLoggedIn()
    }
    
    /**
     * Logout user
     */
    fun logout() {
        authRepository.logout()
        _uiState.value = LoginUiState()
    }
}

/**
 * UI state for login screen
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val errorMessage: String? = null,
    val userData: com.example.aitoolpoc.network.User? = null
)
