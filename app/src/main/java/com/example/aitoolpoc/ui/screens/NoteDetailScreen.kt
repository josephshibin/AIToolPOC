package com.example.aitoolpoc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aitoolpoc.R
import com.example.aitoolpoc.ui.theme.DiaryColors
import com.example.aitoolpoc.ui.theme.LoginTypography
import com.example.aitoolpoc.viewmodel.NoteDetailViewModel

/**
 * Note Detail Screen showing full note content based on Figma design
 */
@Composable
fun NoteDetailScreen(
    noteId: String,
    onBackPressed: () -> Unit = {},
    onEditNote: (String) -> Unit = {},
    onShareNote: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: NoteDetailViewModel = viewModel { NoteDetailViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Load note details when screen is first displayed
    LaunchedEffect(noteId) {
        viewModel.loadNoteDetails(noteId)
    }
    
    // Handle error messages
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiaryColors.White)
    ) {
        // Header with back button and title
        NoteDetailHeader(
            onBackPressed = onBackPressed,
            onShareNote = onShareNote
        )
        
        // Divider line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFD8D8D8))
        )
        
        // Main content
        when {
            uiState.isLoading -> {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = DiaryColors.TealAccent
                    )
                }
            }
            uiState.note != null -> {
                // Note content
                NoteDetailContent(
                    note = uiState.note!!,
                    noteResponse = uiState.noteResponse,
                    viewModel = viewModel,
                    onEditNote = { onEditNote(noteId) },
                    modifier = Modifier.weight(1f)
                )
            }
            else -> {
                // Error state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Failed to load note details",
                            style = LoginTypography.InputFieldText.copy(
                                fontSize = 16.sp,
                                color = DiaryColors.GrayText
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Retry",
                            style = LoginTypography.InputFieldText.copy(
                                fontSize = 14.sp,
                                color = DiaryColors.TealAccent,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable {
                                viewModel.retryLoadNote(noteId)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Header component with back button, title, and share button
 */
@Composable
private fun NoteDetailHeader(
    onBackPressed: () -> Unit,
    onShareNote: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(DiaryColors.White)
    ) {
        // Back button
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
                .size(24.dp)
                .clickable { onBackPressed() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back_arrow),
                contentDescription = "Back",
                tint = DiaryColors.DarkText,
                modifier = Modifier.size(16.dp)
            )
        }
        
        // Title
        Text(
            text = "NOTES",
            style = LoginTypography.InputFieldText.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DiaryColors.DarkText
            ),
            modifier = Modifier.align(Alignment.Center)
        )
        
        // Home icon (right side)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
                .size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "Home",
                tint = DiaryColors.DarkText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Main note content component based on Figma design
 */
@Composable
private fun NoteDetailContent(
    note: com.example.aitoolpoc.data.DiaryNote,
    noteResponse: com.example.aitoolpoc.repository.NoteResponse?,
    viewModel: NoteDetailViewModel,
    onEditNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 72.dp) // Space for bottom button
        ) {
        // Main note card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(DiaryColors.White)
                .border(
                    width = 0.5.dp,
                    color = Color(0xFFCFD5DA),
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Note title
                Text(
                    text = note.title,
                    style = LoginTypography.InputFieldText.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DiaryColors.DarkText
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Note description/content
                Text(
                    text = note.content,
                    style = LoginTypography.InputFieldText.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = DiaryColors.DarkText,
                        lineHeight = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Image placeholder (if imageId exists)
                noteResponse?.imageId?.let { imageId ->
                    if (imageId.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Image placeholder based on Figma design
                        Box(
                            modifier = Modifier
                                .width(162.dp)
                                .height(222.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Image",
                                style = LoginTypography.InputFieldText.copy(
                                    fontSize = 14.sp,
                                    color = DiaryColors.GrayText
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Share button (top right of content area)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { /* Share functionality */ }
                    ) {
                        Text(
                            text = "Share",
                            style = LoginTypography.InputFieldText.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DiaryColors.TealAccent
                            )
                        )
                    }
                }
            }
        }

            // Date/time info
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = viewModel.getFormattedDate(note.timestamp),
                style = LoginTypography.InputFieldText.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF838E97)
                )
            )
        }

        // Edit Note button fixed at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF007DB2),
                            Color(0xFF00D6AF)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF007DB2),
                            Color(0xFF00D6AF)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .clickable { onEditNote() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Edit Note",
                style = LoginTypography.InputFieldText.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DiaryColors.TealAccent
                )
            )
        }
    }
}
