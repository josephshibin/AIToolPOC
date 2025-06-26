package com.example.aitoolpoc.repository

import android.content.Context
import com.example.aitoolpoc.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Generic repository for all API calls
 */
class GenericApiRepository(context: Context) {
    
    private val apiService = NetworkModule.apiService
    private val authRepository = AuthRepository(context)
    
    /**
     * Generic GET request
     */
    suspend fun get(
        endpoint: String,
        includeAuth: Boolean = true
    ): ApiResult<Any> {
        return withContext(Dispatchers.IO) {
            try {
                val headers = if (includeAuth) authRepository.getAuthHeaders() else emptyMap()
                val response = apiService.get(endpoint, headers)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        ApiResult.Success(apiResponse.data ?: Any())
                    } else {
                        ApiResult.Error(apiResponse?.message ?: "Request failed")
                    }
                } else {
                    handleHttpError(response.code())
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
    
    /**
     * Generic POST request
     */
    suspend fun post(
        endpoint: String,
        body: Any,
        includeAuth: Boolean = true
    ): ApiResult<Any> {
        return withContext(Dispatchers.IO) {
            try {
                val headers = if (includeAuth) authRepository.getAuthHeaders() else emptyMap()
                val response = apiService.post(endpoint, body, headers)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        ApiResult.Success(apiResponse.data ?: Any())
                    } else {
                        ApiResult.Error(apiResponse?.message ?: "Request failed")
                    }
                } else {
                    handleHttpError(response.code())
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
    
    /**
     * Generic PUT request
     */
    suspend fun put(
        endpoint: String,
        body: Any,
        includeAuth: Boolean = true
    ): ApiResult<Any> {
        return withContext(Dispatchers.IO) {
            try {
                val headers = if (includeAuth) authRepository.getAuthHeaders() else emptyMap()
                val response = apiService.put(endpoint, body, headers)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        ApiResult.Success(apiResponse.data ?: Any())
                    } else {
                        ApiResult.Error(apiResponse?.message ?: "Request failed")
                    }
                } else {
                    handleHttpError(response.code())
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
    
    /**
     * Generic DELETE request
     */
    suspend fun delete(
        endpoint: String,
        includeAuth: Boolean = true
    ): ApiResult<Any> {
        return withContext(Dispatchers.IO) {
            try {
                val headers = if (includeAuth) authRepository.getAuthHeaders() else emptyMap()
                val response = apiService.delete(endpoint, headers)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        ApiResult.Success(apiResponse.data ?: Any())
                    } else {
                        ApiResult.Error(apiResponse?.message ?: "Request failed")
                    }
                } else {
                    handleHttpError(response.code())
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
    
    /**
     * Handle HTTP error responses
     */
    private fun handleHttpError(code: Int): ApiResult.Error {
        val message = when (code) {
            401 -> "Unauthorized. Please login again"
            403 -> "Access forbidden"
            404 -> "Resource not found"
            500 -> "Server error. Please try again later"
            502, 503 -> "Service unavailable. Please try again later"
            else -> "Request failed with code: $code"
        }
        return ApiResult.Error(message, code)
    }
    
    /**
     * Handle exceptions
     */
    private fun handleException(e: Exception): ApiResult.Error {
        return when (e) {
            is HttpException -> ApiResult.Error("Network error: ${e.message}", e.code())
            is IOException -> ApiResult.Error("Connection error. Please check your internet connection")
            else -> ApiResult.Error("An unexpected error occurred: ${e.message}")
        }
    }
}
