package com.example.aitoolpoc.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.aitoolpoc.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository for authentication operations
 */
class AuthRepository(private val context: Context) {
    
    private val apiService = NetworkModule.apiService
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_AUTHORIZATION = "authorization"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_MEDICAL_PROFILE_ID = "medical_profile_id"
    }
    
    /**
     * Login with email and password
     */
    suspend fun loginWithEmail(email: String, password: String): ApiResult<LoginData> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest.createEmailLogin(email, password)
                val response = apiService.login(request)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        // Save authorization token and user data
                        saveAuthData(apiResponse.data)
                        ApiResult.Success(apiResponse.data)
                    } else {
                        ApiResult.Error(apiResponse?.message ?: "Login failed")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Invalid credentials"
                        404 -> "User not found"
                        500 -> "Server error. Please try again later"
                        else -> "Login failed. Please try again"
                    }
                    ApiResult.Error(errorMessage, response.code())
                }
            } catch (e: HttpException) {
                ApiResult.Error("Network error: ${e.message}", e.code())
            } catch (e: IOException) {
                ApiResult.Error("Connection error. Please check your internet connection")
            } catch (e: Exception) {
                ApiResult.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }
    
    /**
     * Login with signup code and password
     */
    suspend fun loginWithCode(signUpCode: String, password: String): ApiResult<LoginData> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest.createCodeLogin(signUpCode, password)
                val response = apiService.login(request)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        // Save authorization token and user data
                        saveAuthData(apiResponse.data)
                        ApiResult.Success(apiResponse.data)
                    } else {
                        ApiResult.Error(apiResponse?.message ?: "Login failed")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Invalid signup code or password"
                        404 -> "Signup code not found"
                        500 -> "Server error. Please try again later"
                        else -> "Login failed. Please try again"
                    }
                    ApiResult.Error(errorMessage, response.code())
                }
            } catch (e: HttpException) {
                ApiResult.Error("Network error: ${e.message}", e.code())
            } catch (e: IOException) {
                ApiResult.Error("Connection error. Please check your internet connection")
            } catch (e: Exception) {
                ApiResult.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }
    
    /**
     * Save authentication data to SharedPreferences
     */
    private fun saveAuthData(loginData: LoginData) {
        android.util.Log.d("AuthRepository", "Saving auth data:")
        android.util.Log.d("AuthRepository", "Authorization: ${loginData.authorization}")
        android.util.Log.d("AuthRepository", "_id (User ID = Medical Profile ID): ${loginData.user._id}")
        android.util.Log.d("AuthRepository", "User Name: ${loginData.user.fullName}")
        android.util.Log.d("AuthRepository", "Email: ${loginData.user.email}")

        sharedPreferences.edit().apply {
            putString(KEY_AUTHORIZATION, loginData.authorization)
            putString(KEY_USER_DATA, com.google.gson.Gson().toJson(loginData.user))
            // _id from login response = medical-profile-id for notes API
            putString(KEY_MEDICAL_PROFILE_ID, loginData.user._id)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }

        android.util.Log.d("AuthRepository", "Auth data saved successfully")
        android.util.Log.d("AuthRepository", "Medical Profile ID for notes API: ${loginData.user._id}")
    }
    
    /**
     * Get saved authorization token
     */
    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTHORIZATION, null)
    }
    
    /**
     * Get saved user data
     */
    fun getUserData(): User? {
        val userJson = sharedPreferences.getString(KEY_USER_DATA, null)
        return if (userJson != null) {
            try {
                com.google.gson.Gson().fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) && 
               getAuthToken() != null
    }
    
    /**
     * Logout user
     */
    fun logout() {
        sharedPreferences.edit().clear().apply()
    }
    
    /**
     * Get saved medical profile ID (same as user._id from login response)
     */
    fun getMedicalProfileId(): String? {
        val medicalProfileId = sharedPreferences.getString(KEY_MEDICAL_PROFILE_ID, null)
        android.util.Log.d("AuthRepository", "Retrieved Medical Profile ID: $medicalProfileId")
        return medicalProfileId
    }

    /**
     * Get authorization headers for API calls
     */
    fun getAuthHeaders(): Map<String, String> {
        val token = getAuthToken()
        return if (token != null) {
            mapOf("authorization-token" to "$token")
        } else {
            emptyMap()
        }
    }
}
