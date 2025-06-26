package com.example.aitoolpoc.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aitoolpoc.network.ApiResult
import com.example.aitoolpoc.repository.AuthRepository
import com.example.aitoolpoc.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Add Note screen operations
 */
class AddNoteViewModel(context: Context) : ViewModel() {
    
    private val authRepository = AuthRepository(context)
    private val notesRepository = NotesRepository(context)
    
    private val _uiState = MutableStateFlow(AddNoteUiState())
    val uiState: StateFlow<AddNoteUiState> = _uiState.asStateFlow()
    
    /**
     * Save a new note or update existing note
     */
    fun saveNote(title: String, description: String, imageId: String? = null, editNoteId: String? = null) {
        // Validate inputs
        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Title is required"
            )
            return
        }
        
        if (description.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Description is required"
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            val result = if (editNoteId != null) {
                // Edit existing note
                android.util.Log.d("AddNoteViewModel", "Updating existing note: $editNoteId")
                notesRepository.updateNote(
                    noteId = editNoteId,
                    title = title,
                    description = description,
                    imageId = imageId
                )
            } else {
                // Create new note
                android.util.Log.d("AddNoteViewModel", "Creating new note")
                notesRepository.createNote(
                    title = title,
                    description = description,
                    imageId = imageId
                )
            }

            when (result) {
                is ApiResult.Success -> {
                    android.util.Log.d("AddNoteViewModel", "=== SAVE SUCCESS ===")
                    android.util.Log.d("AddNoteViewModel", "Note ${if (editNoteId != null) "updated" else "created"} successfully")
                    android.util.Log.d("AddNoteViewModel", "Setting isSaveSuccessful = true")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSaveSuccessful = true
                    )
                    android.util.Log.d("AddNoteViewModel", "State updated - isSaveSuccessful: ${_uiState.value.isSaveSuccessful}")
                }
                is ApiResult.Error -> {
                    android.util.Log.e("AddNoteViewModel", "=== SAVE ERROR ===")
                    android.util.Log.e("AddNoteViewModel", "Failed to ${if (editNoteId != null) "update" else "create"} note: ${result.message}")
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
     * Upload image and get image ID
     */
    fun uploadImage(imageUri: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )
        
        viewModelScope.launch {
            when (val result = notesRepository.uploadImage(imageUri)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        uploadedImageId = result.data as? String
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
     * Reset save state when screen is opened
     */
    fun resetSaveState() {
        android.util.Log.d("AddNoteViewModel", "=== RESETTING SAVE STATE ===")
        android.util.Log.d("AddNoteViewModel", "Previous state - isSaveSuccessful: ${_uiState.value.isSaveSuccessful}")
        android.util.Log.d("AddNoteViewModel", "Previous state - isLoading: ${_uiState.value.isLoading}")
        android.util.Log.d("AddNoteViewModel", "Previous state - errorMessage: ${_uiState.value.errorMessage}")

        _uiState.value = AddNoteUiState(
            isLoading = false,
            isSaveSuccessful = false,
            errorMessage = null,
            uploadedImageId = null,
            loadedNoteData = null
        )

        android.util.Log.d("AddNoteViewModel", "State reset complete - isSaveSuccessful: ${_uiState.value.isSaveSuccessful}")
    }

    /**
     * Load note data for editing
     */
    fun loadNoteForEdit(noteId: String) {
        android.util.Log.d("AddNoteViewModel", "Loading note data for edit: $noteId")
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            when (val result = notesRepository.getNoteById(noteId)) {
                is ApiResult.Success -> {
                    android.util.Log.d("AddNoteViewModel", "Note data loaded successfully")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loadedNoteData = result.data,
                        errorMessage = null
                    )
                }
                is ApiResult.Error -> {
                    android.util.Log.e("AddNoteViewModel", "Failed to load note data: ${result.message}")
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
     * Check if user is logged in
     */
    fun checkLoginStatus(): Boolean {
        return authRepository.isLoggedIn()
    }
}

/**
 * UI state for Add Note screen
 */
data class AddNoteUiState(
    val isLoading: Boolean = false,
    val isSaveSuccessful: Boolean = false,
    val errorMessage: String? = null,
    val uploadedImageId: String? = null,
    val loadedNoteData: com.example.aitoolpoc.repository.NoteResponse? = null
)
