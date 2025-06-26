package com.example.aitoolpoc.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aitoolpoc.data.DiaryNote
import com.example.aitoolpoc.network.ApiResult
import com.example.aitoolpoc.repository.AuthRepository
import com.example.aitoolpoc.repository.NotesRepository
import com.example.aitoolpoc.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Diary screen operations
 */
class DiaryViewModel(context: Context) : ViewModel() {
    
    private val authRepository = AuthRepository(context)
    private val notesRepository = NotesRepository(context)
    
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()
    
    init {
        // Load notes when ViewModel is created
        loadNotes()
    }
    
    /**
     * Load all notes from API
     */
    fun loadNotes(limit: Int = 20, start: Int = 0) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )
        
        viewModelScope.launch {
            when (val result = notesRepository.getNotes(limit, start)) {
                is ApiResult.Success -> {
                    val notesListResponse = result.data
                    android.util.Log.d("DiaryViewModel", "API Success: received ${notesListResponse.notes.size} notes")
                    android.util.Log.d("DiaryViewModel", "Notes from API: ${notesListResponse.notes}")

                    val groupedNotes = DateUtils.groupNotesByDate(notesListResponse.notes)
                    android.util.Log.d("DiaryViewModel", "After grouping: ${groupedNotes.size} date groups")
                    android.util.Log.d("DiaryViewModel", "Grouped notes: $groupedNotes")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        groupedNotes = groupedNotes,
                        totalNotes = notesListResponse.total,
                        hasMoreNotes = (notesListResponse.start + notesListResponse.size) < notesListResponse.total
                    )

                    android.util.Log.d("DiaryViewModel", "UI State updated. Total notes: ${notesListResponse.total}")
                    android.util.Log.d("DiaryViewModel", "Final UI state: ${_uiState.value}")
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                    android.util.Log.e("DiaryViewModel", "Failed to load notes: ${result.message}")
                }
                is ApiResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    /**
     * Refresh notes (pull to refresh or after note creation)
     */
    fun refreshNotes() {
        android.util.Log.d("DiaryViewModel", "=== Refreshing Notes ===")
        android.util.Log.d("DiaryViewModel", "Triggered by user action or note creation")
        loadNotes()
    }

    /**
     * Force refresh notes (called after note creation/deletion)
     */
    fun forceRefresh() {
        android.util.Log.d("DiaryViewModel", "=== Force Refreshing Notes ===")
        android.util.Log.d("DiaryViewModel", "Triggered by note creation or deletion")
        loadNotes()
    }
    
    /**
     * Load more notes (pagination)
     */
    fun loadMoreNotes() {
        val currentState = _uiState.value
        if (currentState.isLoading || !currentState.hasMoreNotes) {
            return
        }
        
        val currentNotesCount = currentState.groupedNotes.values.flatten().size
        loadNotes(limit = 20, start = currentNotesCount)
    }
    
    /**
     * Delete a note using the API
     */
    fun deleteNote(noteId: String) {
        android.util.Log.d("DiaryViewModel", "=== Delete Note Requested ===")
        android.util.Log.d("DiaryViewModel", "Note ID to delete: $noteId")

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            when (val result = notesRepository.deleteNote(noteId)) {
                is ApiResult.Success -> {
                    android.util.Log.d("DiaryViewModel", "Note deleted successfully from API: $noteId")

                    // Remove note from current state and regroup
                    val currentNotes = _uiState.value.groupedNotes.values.flatten()
                    android.util.Log.d("DiaryViewModel", "Current notes before deletion: ${currentNotes.size}")

                    val updatedNotes = currentNotes.filter { it.id != noteId }
                    android.util.Log.d("DiaryViewModel", "Notes after filtering: ${updatedNotes.size}")

                    val regroupedNotes = if (updatedNotes.isNotEmpty()) {
                        DateUtils.groupNotesByDate(updatedNotes.map { diaryNote ->
                            // Convert DiaryNote back to NoteResponse for grouping
                            com.example.aitoolpoc.repository.NoteResponse(
                                id = diaryNote.id,
                                title = diaryNote.title,
                                description = diaryNote.content,
                                imageId = diaryNote.imageId,
                                medicalProfileId = null,
                                createdAt = diaryNote.timestamp,
                                updatedAt = diaryNote.timestamp,
                                imageUrl = null
                            )
                        })
                    } else {
                        emptyMap()
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        groupedNotes = regroupedNotes,
                        totalNotes = maxOf(0, _uiState.value.totalNotes - 1)
                    )

                    android.util.Log.d("DiaryViewModel", "UI state updated after deletion. Remaining notes: ${regroupedNotes.values.flatten().size}")
                }
                is ApiResult.Error -> {
                    android.util.Log.e("DiaryViewModel", "Failed to delete note: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to delete note: ${result.message}"
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
 * UI state for Diary screen
 */
data class DiaryUiState(
    val isLoading: Boolean = false,
    val groupedNotes: Map<String, List<DiaryNote>> = emptyMap(),
    val totalNotes: Int = 0,
    val hasMoreNotes: Boolean = false,
    val errorMessage: String? = null
)
