package com.example.aitoolpoc.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aitoolpoc.data.DiaryNote
import com.example.aitoolpoc.network.ApiResult
import com.example.aitoolpoc.repository.AuthRepository
import com.example.aitoolpoc.repository.NotesRepository
import com.example.aitoolpoc.repository.NoteResponse
import com.example.aitoolpoc.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Note Detail screen operations
 */
class NoteDetailViewModel(context: Context) : ViewModel() {
    
    private val authRepository = AuthRepository(context)
    private val notesRepository = NotesRepository(context)
    
    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()
    
    /**
     * Load note details by ID
     */
    fun loadNoteDetails(noteId: String) {
        android.util.Log.d("NoteDetailViewModel", "=== Loading Note Details ===")
        android.util.Log.d("NoteDetailViewModel", "Note ID: $noteId")
        
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )
        
        viewModelScope.launch {
            when (val result = notesRepository.getNoteById(noteId)) {
                is ApiResult.Success -> {
                    val noteResponse = result.data
                    android.util.Log.d("NoteDetailViewModel", "Note details loaded successfully: ${noteResponse.title}")
                    
                    // Convert NoteResponse to DiaryNote for UI
                    val diaryNote = DiaryNote(
                        id = noteResponse.id,
                        title = noteResponse.title,
                        content = noteResponse.description,
                        timestamp = noteResponse.createdAt,
                        imageId = noteResponse.imageId
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        note = diaryNote,
                        noteResponse = noteResponse,
                        errorMessage = null
                    )
                    
                    android.util.Log.d("NoteDetailViewModel", "UI state updated with note details")
                }
                is ApiResult.Error -> {
                    android.util.Log.e("NoteDetailViewModel", "Failed to load note details: ${result.message}")
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
     * Retry loading note details
     */
    fun retryLoadNote(noteId: String) {
        loadNoteDetails(noteId)
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Get formatted date for display
     */
    fun getFormattedDate(timestamp: Long): String {
        return when {
            DateUtils.isToday(timestamp) -> "Today, ${DateUtils.formatTime(timestamp)}"
            DateUtils.isYesterday(timestamp) -> "Yesterday, ${DateUtils.formatTime(timestamp)}"
            else -> "${DateUtils.formatDate(timestamp)}, ${DateUtils.formatTime(timestamp)}"
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun checkLoginStatus(): Boolean {
        return authRepository.isLoggedIn()
    }
    
    /**
     * Get user info for display
     */
    fun getUserInfo(): String? {
        val userData = authRepository.getUserData()
        return userData?.fullName
    }
}

/**
 * UI state for Note Detail screen
 */
data class NoteDetailUiState(
    val isLoading: Boolean = false,
    val note: DiaryNote? = null,
    val noteResponse: NoteResponse? = null,
    val errorMessage: String? = null
)
