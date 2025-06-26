package com.example.aitoolpoc

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.aitoolpoc.ui.screens.AddNoteScreen
import com.example.aitoolpoc.ui.screens.LoginScreen
import com.example.aitoolpoc.ui.screens.EmailLoginScreen
import com.example.aitoolpoc.ui.screens.DiaryMainScreen
import com.example.aitoolpoc.ui.screens.NoteDetailScreen
import com.example.aitoolpoc.ui.theme.AIToolPOCTheme

enum class ScreenType {
    INVITATION_CODE,
    EMAIL_LOGIN,
    DIARY_MAIN,
    ADD_NOTE,
    NOTE_DETAIL
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIToolPOCTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginNavigationScreen()
                }
            }
        }
    }
}

@Composable
fun LoginNavigationScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val authRepository = remember { com.example.aitoolpoc.repository.AuthRepository(context) }

    // Always start with login screen (as requested)
    var currentScreen by remember { mutableStateOf(ScreenType.INVITATION_CODE) }
    var selectedNoteId by remember { mutableStateOf<String?>(null) }
    var shouldRefreshDiary by remember { mutableStateOf(false) }

    // Edit note state
    var editNoteId by remember { mutableStateOf<String?>(null) }
    var editNoteTitle by remember { mutableStateOf("") }
    var editNoteDescription by remember { mutableStateOf("") }

    when (currentScreen) {
        ScreenType.INVITATION_CODE -> {
            LoginScreen(
                onBackPressed = {
                    // Handle back navigation - could exit app or go to previous screen
                },
                onLoginSuccess = {
                    // Navigate to diary screen on successful login
                    currentScreen = ScreenType.DIARY_MAIN
                },
                onForgotPinPressed = {
                    // Handle forgot PIN
                    Toast.makeText(context, "Forgot PIN clicked", Toast.LENGTH_SHORT).show()
                },
                onEmailLoginPressed = {
                    // Navigate to email login screen
                    currentScreen = ScreenType.EMAIL_LOGIN
                }
            )
        }

        ScreenType.EMAIL_LOGIN -> {
            EmailLoginScreen(
                onBackPressed = {
                    // Go back to invitation code screen
                    currentScreen = ScreenType.INVITATION_CODE
                },
                onLoginSuccess = {
                    // Navigate to diary screen on successful login
                    currentScreen = ScreenType.DIARY_MAIN
                },
                onForgotPinPressed = {
                    // Handle forgot PIN
                    Toast.makeText(context, "Forgot PIN clicked", Toast.LENGTH_SHORT).show()
                }
            )
        }

        ScreenType.DIARY_MAIN -> {
            DiaryMainScreen(
                onBackPressed = {
                    // Logout and go back to login screen
                    authRepository.logout()
                    currentScreen = ScreenType.INVITATION_CODE
                },
                onNoteClick = { note ->
                    // Navigate to Note Detail screen
                    selectedNoteId = note.id
                    currentScreen = ScreenType.NOTE_DETAIL
                },
                onEditNote = { note ->
                    // Navigate to Add Note screen in edit mode
                    editNoteId = note.id
                    editNoteTitle = note.title
                    editNoteDescription = note.content
                    shouldRefreshDiary = false  // Reset refresh trigger
                    currentScreen = ScreenType.ADD_NOTE
                },
                onDeleteNote = { note ->
                    Toast.makeText(context, "Deleted note: ${note.title}", Toast.LENGTH_SHORT).show()
                },
                onAddNewNote = {
                    // Navigate to Add Note screen
                    shouldRefreshDiary = false  // Reset refresh trigger
                    currentScreen = ScreenType.ADD_NOTE
                },
                refreshTrigger = shouldRefreshDiary  // Pass refresh trigger
            )

            // Reset refresh trigger after it's been processed
            LaunchedEffect(shouldRefreshDiary) {
                if (shouldRefreshDiary) {
                    // Reset after a short delay to ensure the refresh is processed
                    kotlinx.coroutines.delay(100)
                    shouldRefreshDiary = false
                }
            }
        }

        ScreenType.ADD_NOTE -> {
            AddNoteScreen(
                onBackPressed = {
                    // Go back to diary screen without refreshing and clear edit state
                    editNoteId = null
                    editNoteTitle = ""
                    editNoteDescription = ""
                    currentScreen = ScreenType.DIARY_MAIN
                },
                onNoteSaved = {
                    // Navigate back to diary screen after successful save and trigger refresh
                    val message = if (editNoteId != null) "Note updated successfully!" else "Note saved successfully!"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    shouldRefreshDiary = true  // Trigger diary refresh
                    // Clear edit state
                    editNoteId = null
                    editNoteTitle = ""
                    editNoteDescription = ""
                    currentScreen = ScreenType.DIARY_MAIN
                },
                onCancelPressed = {
                    // Go back to diary screen without saving or refreshing and clear edit state
                    editNoteId = null
                    editNoteTitle = ""
                    editNoteDescription = ""
                    currentScreen = ScreenType.DIARY_MAIN
                },
                editNoteId = editNoteId,
                editTitle = editNoteTitle,
                editDescription = editNoteDescription
            )
        }

        ScreenType.NOTE_DETAIL -> {
            selectedNoteId?.let { noteId ->
                NoteDetailScreen(
                    noteId = noteId,
                    onBackPressed = {
                        // Go back to diary screen
                        currentScreen = ScreenType.DIARY_MAIN
                        selectedNoteId = null
                    },
                    onEditNote = { noteIdToEdit ->
                        // For edit from detail screen, we need to get the note data
                        // Since we don't have direct access to the note data here,
                        // we'll pass the noteId and let AddNoteScreen fetch the data
                        editNoteId = noteIdToEdit
                        editNoteTitle = ""  // Will be loaded by AddNoteScreen
                        editNoteDescription = ""  // Will be loaded by AddNoteScreen
                        shouldRefreshDiary = false  // Reset refresh trigger
                        currentScreen = ScreenType.ADD_NOTE
                    },
                    onShareNote = {
                        // Handle share functionality
                        Toast.makeText(context, "Share note", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}