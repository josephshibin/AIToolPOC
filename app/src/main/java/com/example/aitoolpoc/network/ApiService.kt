package com.example.aitoolpoc.network

import retrofit2.Response
import retrofit2.http.*

/**
 * Generic API service interface for all API calls
 */
interface ApiService {
    
    /**
     * Login API endpoint
     */
    @POST("v1/authenticate-by-signup-code-or-email")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginData>>
    
    /**
     * Generic GET request
     */
    @GET
    suspend fun get(
        @Url url: String,
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): Response<ApiResponse<Any>>
    
    /**
     * Generic POST request
     */
    @POST
    suspend fun post(
        @Url url: String,
        @Body body: Any,
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): Response<ApiResponse<Any>>
    
    /**
     * Generic PUT request
     */
    @PUT
    suspend fun put(
        @Url url: String,
        @Body body: Any,
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): Response<ApiResponse<Any>>
    
    /**
     * Generic DELETE request
     */
    @DELETE
    suspend fun delete(
        @Url url: String,
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): Response<ApiResponse<Any>>
}

/**
 * Login request data classes
 */
data class LoginRequest(
    val email: String? = null,
    val password: String? = null,
    val signUpCode: String? = null
) {
    companion object {
        fun createEmailLogin(email: String, password: String) = LoginRequest(
            email = email,
            password = password
        )
        
        fun createCodeLogin(signUpCode: String, password: String) = LoginRequest(
            signUpCode = signUpCode,
            password = password
        )
    }
}

/**
 * Generic API response wrapper
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null
)

/**
 * Login response data
 */
data class LoginData(
    val authorization: String,
    val user: User
)

/**
 * User data model
 * Note: _id field is used as medical-profile-id for notes API
 */
data class User(
    val _id: String,           // This is the medical-profile-id for notes API
    val roleId: String,
    val profileImageId: String,
    val fullName: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val roleName: String
)
