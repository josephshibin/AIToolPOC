package com.example.aitoolpoc.repository

import android.content.Context
import com.example.aitoolpoc.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository for notes operations
 */
class NotesRepository(context: Context) {
    
    private val apiService = NetworkModule.apiService
    private val authRepository = AuthRepository(context)
    
    /**
     * Create a new note using saved medical profile ID
     */
    suspend fun createNote(
        title: String,
        description: String,
        imageId: String? = ""
    ): ApiResult<Any> {
        return withContext(Dispatchers.IO) {
            try {
                val headers = authRepository.getAuthHeaders()
                if (headers.isEmpty()) {
                    return@withContext ApiResult.Error("User not authenticated. Please login again.")
                }

                // Get medical profile ID from saved auth data (user._id from login response)
                val medicalProfileId = authRepository.getMedicalProfileId()
                if (medicalProfileId == null) {
                    return@withContext ApiResult.Error("Medical profile ID not found. Please login again.")
                }

                val endpoint = "v1/medical-profiles/$medicalProfileId/notes"
                android.util.Log.d("NotesRepository", "=== Creating Note ===")
                android.util.Log.d("NotesRepository", "API Endpoint: $endpoint")
                android.util.Log.d("NotesRepository", "Medical Profile ID (user._id): $medicalProfileId")
                android.util.Log.d("NotesRepository", "Title: $title")
                android.util.Log.d("NotesRepository", "Description: $description")
                android.util.Log.d("NotesRepository", "ImageId: $imageId")
                val requestBody = CreateNoteRequest(
                    title = title,
                    description = description,
                    imageId = imageId ?: ""  // Ensure empty string instead of null
                )

                android.util.Log.d("NotesRepository", "Request Headers: $headers")
                android.util.Log.d("NotesRepository", "Request Body: $requestBody")

                val response = apiService.post(endpoint, requestBody, headers)

                android.util.Log.d("NotesRepository", "Response Code: ${response.code()}")
                android.util.Log.d("NotesRepository", "Response Success: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    android.util.Log.d("NotesRepository", "Response Body: $apiResponse")
                    if (apiResponse?.success == true) {
                        android.util.Log.d("NotesRepository", "Note created successfully!")
                        ApiResult.Success(apiResponse.data ?: Any())
                    } else {
                        android.util.Log.e("NotesRepository", "API returned success=false: ${apiResponse?.message}")
                        ApiResult.Error(apiResponse?.message ?: "Failed to create note")
                    }
                } else {
                    android.util.Log.e("NotesRepository", "HTTP Error: ${response.code()}")
                    handleHttpError(response.code())
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
    
    /**
     * Upload image and get image ID
     */
    suspend fun uploadImage(imageUri: String): ApiResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val headers = authRepository.getAuthHeaders()
                if (headers.isEmpty()) {
                    return@withContext ApiResult.Error("User not authenticated. Please login again.")
                }
                
                // TODO: Implement actual image upload
                // For now, return a mock image ID
                ApiResult.Success("mock-image-id-${System.currentTimeMillis()}")
                
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
    
    /**
     * Get all notes for the current user's medical profile
     */
    suspend fun getNotes(limit: Int = 20, start: Int = 0): ApiResult<NotesListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val headers = authRepository.getAuthHeaders()
                if (headers.isEmpty()) {
                    return@withContext ApiResult.Error("User not authenticated. Please login again.")
                }

                // Get medical profile ID from saved auth data
                val medicalProfileId = authRepository.getMedicalProfileId()
                if (medicalProfileId == null) {
                    return@withContext ApiResult.Error("Medical profile not found. Please login again.")
                }

                val endpoint = "v1/medical-profiles/$medicalProfileId/notes?limit=$limit&start=$start"

                android.util.Log.d("NotesRepository", "=== Fetching Notes ===")
                android.util.Log.d("NotesRepository", "API Endpoint: $endpoint")
                android.util.Log.d("NotesRepository", "Medical Profile ID (user._id): $medicalProfileId")
                android.util.Log.d("NotesRepository", "Pagination: limit=$limit, start=$start")
                android.util.Log.d("NotesRepository", "Request Headers: $headers")

                val response = apiService.get(endpoint, headers)

                android.util.Log.d("NotesRepository", "Response Code: ${response.code()}")
                android.util.Log.d("NotesRepository", "Response Success: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    android.util.Log.d("NotesRepository", "Raw Response Body: $apiResponse")
                    android.util.Log.d("NotesRepository", "Response Success Field: ${apiResponse?.success}")
                    android.util.Log.d("NotesRepository", "Response Data Field: ${apiResponse?.data}")

                    if (apiResponse?.success == true) {
                        // Parse the response data as notes list
                        android.util.Log.d("NotesRepository", "Starting to parse response data...")
                        val notesListResponse = parseNotesListResponse(apiResponse.data)
                        android.util.Log.d("NotesRepository", "Parsing completed. Found ${notesListResponse.notes.size} notes")
                        android.util.Log.d("NotesRepository", "Notes details: ${notesListResponse.notes}")
                        ApiResult.Success(notesListResponse)
                    } else {
                        android.util.Log.e("NotesRepository", "API returned success=false: ${apiResponse?.message}")
                        android.util.Log.e("NotesRepository", "Full response: $apiResponse")
                        ApiResult.Error(apiResponse?.message ?: "Failed to fetch notes")
                    }
                } else {
                    android.util.Log.e("NotesRepository", "HTTP Error: ${response.code()}")
                    android.util.Log.e("NotesRepository", "Error Body: ${response.errorBody()?.string()}")
                    handleHttpError(response.code())
                }
            } catch (e: Exception) {
                android.util.Log.e("NotesRepository", "Exception while fetching notes", e)
                handleException(e)
            }
        }
    }
    
    /**
     * Update an existing note
     */
    suspend fun updateNote(
        noteId: String,
        title: String,
        description: String,
        imageId: String? = ""
    ): ApiResult<Any> {
        return withContext(Dispatchers.IO) {
            try {
                val headers = authRepository.getAuthHeaders()
                if (headers.isEmpty()) {
                    return@withContext ApiResult.Error("User not authenticated. Please login again.")
                }

                // Get medical profile ID from saved auth data (user._id from login response)
                val medicalProfileId = authRepository.getMedicalProfileId()
                if (medicalProfileId == null) {
                    return@withContext ApiResult.Error("Medical profile ID not found. Please login again.")
                }

                val endpoint = "v1/medical-profiles/$medicalProfileId/notes/$noteId"
                android.util.Log.d("NotesRepository", "=== Updating Note ===")
                android.util.Log.d("NotesRepository", "API Endpoint: $endpoint")
                android.util.Log.d("NotesRepository", "Medical Profile ID (user._id): $medicalProfileId")
                android.util.Log.d("NotesRepository", "Note ID: $noteId")
                android.util.Log.d("NotesRepository", "Title: $title")
                android.util.Log.d("NotesRepository", "Description: $description")
                android.util.Log.d("NotesRepository", "ImageId: $imageId")

                val requestBody = UpdateNoteRequest(
                    title = title,
                    description = description,
                    imageId = imageId ?: ""  // Ensure empty string instead of null
                )

                android.util.Log.d("NotesRepository", "Request Headers: $headers")
                android.util.Log.d("NotesRepository", "Request Body: $requestBody")

                val response = apiService.put(endpoint, requestBody, headers)

                android.util.Log.d("NotesRepository", "Response Code: ${response.code()}")
                android.util.Log.d("NotesRepository", "Response Success: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    android.util.Log.d("NotesRepository", "Response Body: $apiResponse")

                    if (apiResponse?.success == true) {
                        android.util.Log.d("NotesRepository", "Note updated successfully!")
                        ApiResult.Success(apiResponse.data ?: Any())
                    } else {
                        android.util.Log.e("NotesRepository", "API returned success=false: ${apiResponse?.message}")
                        ApiResult.Error(apiResponse?.message ?: "Failed to update note")
                    }
                } else {
                    android.util.Log.e("NotesRepository", "HTTP Error: ${response.code()}")
                    android.util.Log.e("NotesRepository", "Error Body: ${response.errorBody()?.string()}")
                    handleHttpError(response.code())
                }
            } catch (e: Exception) {
                android.util.Log.e("NotesRepository", "Exception while updating note", e)
                handleException(e)
            }
        }
    }

    /**
     * Get a specific note by ID
     */
    suspend fun getNoteById(noteId: String): ApiResult<NoteResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val headers = authRepository.getAuthHeaders()
                if (headers.isEmpty()) {
                    return@withContext ApiResult.Error("User not authenticated. Please login again.")
                }

                // Get medical profile ID from saved auth data
                val medicalProfileId = authRepository.getMedicalProfileId()
                if (medicalProfileId == null) {
                    return@withContext ApiResult.Error("Medical profile not found. Please login again.")
                }

                val endpoint = "v1/medical-profiles/$medicalProfileId/notes/$noteId"

                android.util.Log.d("NotesRepository", "=== Fetching Note Details ===")
                android.util.Log.d("NotesRepository", "API Endpoint: $endpoint")
                android.util.Log.d("NotesRepository", "Medical Profile ID: $medicalProfileId")
                android.util.Log.d("NotesRepository", "Note ID: $noteId")
                android.util.Log.d("NotesRepository", "Request Headers: $headers")

                val response = apiService.get(endpoint, headers)

                android.util.Log.d("NotesRepository", "Response Code: ${response.code()}")
                android.util.Log.d("NotesRepository", "Response Success: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    android.util.Log.d("NotesRepository", "Response Body: $apiResponse")

                    if (apiResponse?.success == true) {
                        // Parse the single note response
                        val noteResponse = parseSingleNoteResponse(apiResponse.data)
                        if (noteResponse != null) {
                            android.util.Log.d("NotesRepository", "Note details fetched successfully: ${noteResponse.title}")
                            ApiResult.Success(noteResponse)
                        } else {
                            android.util.Log.e("NotesRepository", "Failed to parse note response")
                            ApiResult.Error("Failed to parse note details")
                        }
                    } else {
                        android.util.Log.e("NotesRepository", "API returned success=false: ${apiResponse?.message}")
                        ApiResult.Error(apiResponse?.message ?: "Failed to fetch note details")
                    }
                } else {
                    android.util.Log.e("NotesRepository", "HTTP Error: ${response.code()}")
                    android.util.Log.e("NotesRepository", "Error Body: ${response.errorBody()?.string()}")
                    handleHttpError(response.code())
                }
            } catch (e: Exception) {
                android.util.Log.e("NotesRepository", "Exception while fetching note details", e)
                handleException(e)
            }
        }
    }

    /**
     * Delete a note using the specified API endpoint
     */
    suspend fun deleteNote(noteId: String): ApiResult<Any> {
        return withContext(Dispatchers.IO) {
            try {
                val headers = authRepository.getAuthHeaders()
                if (headers.isEmpty()) {
                    return@withContext ApiResult.Error("User not authenticated. Please login again.")
                }

                // Get medical profile ID from saved auth data
                val medicalProfileId = authRepository.getMedicalProfileId()
                if (medicalProfileId == null) {
                    return@withContext ApiResult.Error("Medical profile not found. Please login again.")
                }

                val endpoint = "v1/medical-profiles/$medicalProfileId/notes/$noteId"

                android.util.Log.d("NotesRepository", "=== Deleting Note ===")
                android.util.Log.d("NotesRepository", "API Endpoint: $endpoint")
                android.util.Log.d("NotesRepository", "Medical Profile ID: $medicalProfileId")
                android.util.Log.d("NotesRepository", "Note ID: $noteId")
                android.util.Log.d("NotesRepository", "Request Headers: $headers")

                val response = apiService.delete(endpoint, headers)

                android.util.Log.d("NotesRepository", "Delete Response Code: ${response.code()}")
                android.util.Log.d("NotesRepository", "Delete Response Success: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    android.util.Log.d("NotesRepository", "Delete Response Body: $apiResponse")

                    if (apiResponse?.success == true) {
                        android.util.Log.d("NotesRepository", "Note deleted successfully!")
                        ApiResult.Success(apiResponse.data ?: Any())
                    } else {
                        android.util.Log.e("NotesRepository", "API returned success=false: ${apiResponse?.message}")
                        ApiResult.Error(apiResponse?.message ?: "Failed to delete note")
                    }
                } else {
                    android.util.Log.e("NotesRepository", "HTTP Error: ${response.code()}")
                    android.util.Log.e("NotesRepository", "Error Body: ${response.errorBody()?.string()}")
                    handleHttpError(response.code())
                }
            } catch (e: Exception) {
                android.util.Log.e("NotesRepository", "Exception while deleting note", e)
                handleException(e)
            }
        }
    }
    
    /**
     * Parse notes list response from API
     */
    private fun parseNotesListResponse(data: Any?): NotesListResponse {
        try {
            android.util.Log.d("NotesRepository", "parseNotesListResponse called with data: $data")
            android.util.Log.d("NotesRepository", "Data type: ${data?.javaClass?.simpleName}")

            if (data == null) {
                android.util.Log.w("NotesRepository", "Response data is null")
                return NotesListResponse(emptyList(), 0, 0, 0, 0)
            }

            // Convert data to Map for parsing
            val dataMap = when (data) {
                is Map<*, *> -> {
                    android.util.Log.d("NotesRepository", "Data is Map with keys: ${data.keys}")
                    data as Map<String, Any?>
                }
                else -> {
                    android.util.Log.e("NotesRepository", "Unexpected data type: ${data::class.java}")
                    android.util.Log.e("NotesRepository", "Data content: $data")
                    return NotesListResponse(emptyList(), 0, 0, 0, 0)
                }
            }

            val limit = (dataMap["limit"] as? Number)?.toInt() ?: 20
            val start = (dataMap["start"] as? Number)?.toInt() ?: 0
            val total = (dataMap["total"] as? Number)?.toInt() ?: 0
            val size = (dataMap["size"] as? Number)?.toInt() ?: 0

            android.util.Log.d("NotesRepository", "Pagination info: limit=$limit, start=$start, total=$total, size=$size")

            // Based on actual API response, notes are in "results" field, not "data" field
            val notesData = dataMap["results"] as? List<*> ?: (dataMap["data"] as? List<*> ?: emptyList<Any>())
            android.util.Log.d("NotesRepository", "Notes data array from 'results' field: $notesData")
            android.util.Log.d("NotesRepository", "Notes data size: ${notesData.size}")

            val actualNotesData = notesData

            android.util.Log.d("NotesRepository", "Final notes data to parse: $actualNotesData")
            android.util.Log.d("NotesRepository", "Final notes data size: ${actualNotesData.size}")

            val notes = actualNotesData.mapNotNull { noteItem ->
                android.util.Log.d("NotesRepository", "Parsing note item: $noteItem")
                parseNoteItem(noteItem)
            }

            android.util.Log.d("NotesRepository", "Successfully parsed ${notes.size} notes out of ${notesData.size} items")
            android.util.Log.d("NotesRepository", "Final notes list: $notes")

            // If we still have no notes but the API indicated there should be some, log the issue
            if (notes.isEmpty() && (total > 0 || size > 0)) {
                android.util.Log.w("NotesRepository", "No notes parsed but API indicates notes exist. Check response structure.")
                android.util.Log.w("NotesRepository", "Total: $total, Size: $size, Parsed notes: ${notes.size}")
            }

            return NotesListResponse(notes, limit, start, total, size)

        } catch (e: Exception) {
            android.util.Log.e("NotesRepository", "Error parsing notes response", e)
            android.util.Log.e("NotesRepository", "Exception details: ${e.message}")
            android.util.Log.e("NotesRepository", "Stack trace: ${e.stackTrace.contentToString()}")
            return NotesListResponse(emptyList(), 0, 0, 0, 0)
        }
    }

    /**
     * Parse individual note item from API response
     */
    private fun parseNoteItem(noteItem: Any?): NoteResponse? {
        try {
            android.util.Log.d("NotesRepository", "parseNoteItem called with: $noteItem")

            if (noteItem == null) {
                android.util.Log.w("NotesRepository", "Note item is null")
                return null
            }

            val noteMap = when (noteItem) {
                is Map<*, *> -> {
                    android.util.Log.d("NotesRepository", "Note item is Map with keys: ${noteItem.keys}")
                    noteItem as Map<String, Any?>
                }
                else -> {
                    android.util.Log.w("NotesRepository", "Unexpected note item type: ${noteItem::class.java}")
                    android.util.Log.w("NotesRepository", "Note item content: $noteItem")
                    return null
                }
            }

            val id = noteMap["_id"] as? String
            val title = noteMap["title"] as? String ?: ""
            val description = noteMap["description"] as? String ?: ""
            val imageId = noteMap["imageId"] as? String
            val medicalProfileId = noteMap["medicalProfileId"] as? String
            val createdAt = (noteMap["createdAt"] as? Number)?.toLong() ?: 0L
            val updatedAt = (noteMap["updatedAt"] as? Number)?.toLong() ?: 0L
            val imageUrl = noteMap["imageUrl"] as? String

            android.util.Log.d("NotesRepository", "Extracted fields:")
            android.util.Log.d("NotesRepository", "  _id: $id")
            android.util.Log.d("NotesRepository", "  title: $title")
            android.util.Log.d("NotesRepository", "  description: $description")
            android.util.Log.d("NotesRepository", "  imageId: $imageId")
            android.util.Log.d("NotesRepository", "  medicalProfileId: $medicalProfileId")
            android.util.Log.d("NotesRepository", "  createdAt: $createdAt")
            android.util.Log.d("NotesRepository", "  updatedAt: $updatedAt")
            android.util.Log.d("NotesRepository", "  imageUrl: $imageUrl")

            if (id == null) {
                android.util.Log.e("NotesRepository", "Note ID (_id) is null, skipping note")
                return null
            }

            val noteResponse = NoteResponse(
                id = id,
                title = title,
                description = description,
                imageId = imageId,
                medicalProfileId = medicalProfileId,
                createdAt = createdAt,
                updatedAt = updatedAt,
                imageUrl = imageUrl
            )

            android.util.Log.d("NotesRepository", "Successfully created NoteResponse: $noteResponse")
            return noteResponse

        } catch (e: Exception) {
            android.util.Log.e("NotesRepository", "Error parsing note item", e)
            android.util.Log.e("NotesRepository", "Exception message: ${e.message}")
            return null
        }
    }

    /**
     * Parse single note response from API (for note details)
     */
    private fun parseSingleNoteResponse(data: Any?): NoteResponse? {
        try {
            android.util.Log.d("NotesRepository", "parseSingleNoteResponse called with data: $data")
            android.util.Log.d("NotesRepository", "Data type: ${data?.javaClass?.simpleName}")

            if (data == null) {
                android.util.Log.w("NotesRepository", "Response data is null")
                return null
            }

            // Convert data to Map for parsing
            val dataMap = when (data) {
                is Map<*, *> -> {
                    android.util.Log.d("NotesRepository", "Data is Map with keys: ${data.keys}")
                    data as Map<String, Any?>
                }
                else -> {
                    android.util.Log.e("NotesRepository", "Unexpected data type: ${data::class.java}")
                    android.util.Log.e("NotesRepository", "Data content: $data")
                    return null
                }
            }

            // Parse note fields
            val id = dataMap["_id"] as? String
            val title = dataMap["title"] as? String ?: ""
            val description = dataMap["description"] as? String ?: ""
            val imageId = dataMap["imageId"] as? String
            val medicalProfileId = dataMap["medicalProfileId"] as? String
            val createdAt = (dataMap["createdAt"] as? Number)?.toLong() ?: 0L
            val updatedAt = (dataMap["updatedAt"] as? Number)?.toLong() ?: 0L
            val imageUrl = dataMap["imageUrl"] as? String

            android.util.Log.d("NotesRepository", "Parsed single note fields:")
            android.util.Log.d("NotesRepository", "  _id: $id")
            android.util.Log.d("NotesRepository", "  title: $title")
            android.util.Log.d("NotesRepository", "  description: $description")
            android.util.Log.d("NotesRepository", "  imageId: $imageId")
            android.util.Log.d("NotesRepository", "  medicalProfileId: $medicalProfileId")
            android.util.Log.d("NotesRepository", "  createdAt: $createdAt")
            android.util.Log.d("NotesRepository", "  updatedAt: $updatedAt")
            android.util.Log.d("NotesRepository", "  imageUrl: $imageUrl")

            if (id == null) {
                android.util.Log.e("NotesRepository", "Note ID (_id) is null")
                return null
            }

            val noteResponse = NoteResponse(
                id = id,
                title = title,
                description = description,
                imageId = imageId,
                medicalProfileId = medicalProfileId,
                createdAt = createdAt,
                updatedAt = updatedAt,
                imageUrl = imageUrl
            )

            android.util.Log.d("NotesRepository", "Successfully created single NoteResponse: $noteResponse")
            return noteResponse

        } catch (e: Exception) {
            android.util.Log.e("NotesRepository", "Error parsing single note response", e)
            android.util.Log.e("NotesRepository", "Exception message: ${e.message}")
            return null
        }
    }
    
    /**
     * Handle HTTP error responses
     */
    private fun handleHttpError(code: Int): ApiResult.Error {
        val message = when (code) {
            401 -> "Unauthorized. Please login again"
            403 -> "Access forbidden"
            404 -> "Medical profile or note not found"
            422 -> "Invalid note data. Please check your input"
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

/**
 * Request data class for creating a note
 */
data class CreateNoteRequest(
    val title: String,
    val description: String,
    val imageId: String = ""  // Always send empty string, never null
)

/**
 * Request data class for updating a note
 */
data class UpdateNoteRequest(
    val title: String,
    val description: String,
    val imageId: String = ""  // Always send empty string, never null
)

/**
 * Response data class for individual note
 */
data class NoteResponse(
    val id: String,
    val title: String,
    val description: String,
    val imageId: String? = null,
    val medicalProfileId: String?    = null,  // Made optional as it might not be in response
    val createdAt: Long,        // Timestamp in milliseconds
    val updatedAt: Long,        // Timestamp in milliseconds
    val imageUrl: String? = null // Image URL from API response
)

/**
 * Response data class for notes list with pagination
 */
data class NotesListResponse(
    val notes: List<NoteResponse>,
    val limit: Int,
    val start: Int,
    val total: Int,
    val size: Int
)
