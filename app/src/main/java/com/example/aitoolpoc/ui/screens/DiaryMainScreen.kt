package com.example.aitoolpoc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aitoolpoc.data.DiaryNote
import com.example.aitoolpoc.data.SampleDiaryData
import com.example.aitoolpoc.ui.components.*
import com.example.aitoolpoc.ui.theme.DiaryColors
import com.example.aitoolpoc.ui.theme.LoginTypography
import com.example.aitoolpoc.viewmodel.DiaryViewModel

/**
 * Main diary screen showing list of notes grouped by date
 */
@Composable
fun DiaryMainScreen(
    onBackPressed: () -> Unit = {},
    onNoteClick: (DiaryNote) -> Unit = {},
    onEditNote: (DiaryNote) -> Unit = {},
    onDeleteNote: (DiaryNote) -> Unit = {},
    onAddNewNote: () -> Unit = {},
    refreshTrigger: Boolean = false  // Trigger to refresh notes
) {
    val context = LocalContext.current
    val viewModel: DiaryViewModel = viewModel { DiaryViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    // Handle error messages
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // Handle refresh trigger (when returning from Add Note screen)
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            android.util.Log.d("DiaryMainScreen", "Refresh trigger activated - refreshing notes")
            viewModel.forceRefresh()
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DiaryColors.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with gradient background
            DiaryHeader(onBackPressed = onBackPressed)
            
            // Tab selector (Today/All tabs from design)
            TabSelector(
                modifier = Modifier.padding(16.dp)
            )
            
            // Notes list with loading state
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.isLoading && uiState.groupedNotes.isEmpty()) {
                    // Show loading indicator when initially loading
                    CircularProgressIndicator(
                        color = DiaryColors.TealAccent,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.groupedNotes.isEmpty()) {
                    // Show empty state
                    Text(
                        text = "No notes yet. Tap + to add your first note!",
                        style = LoginTypography.InputFieldText.copy(
                            fontSize = 16.sp,
                            color = DiaryColors.GrayText
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // Show notes list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Iterate through grouped notes from API
                        uiState.groupedNotes.forEach { (dateGroup, notesInGroup) ->
                            // Date header
                            item {
                                DateSectionHeader(dateText = dateGroup)
                            }

                            // Notes in this date group
                            items(notesInGroup) { note ->
                                CustomSwipeableNoteCard(
                                    note = note,
                                    onNoteClick = onNoteClick,
                                    onEditClick = onEditNote,
                                    onDeleteClick = { noteToDelete ->
                                        // Delete note via API
                                        viewModel.deleteNote(noteToDelete.id)
                                        onDeleteNote(noteToDelete)
                                    }
                                )
                            }

                            // Add spacing after each date group
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // Loading indicator for pagination
                        if (uiState.isLoading && uiState.groupedNotes.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = DiaryColors.TealAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Add new note button
            AddNewNoteButton(onClick = onAddNewNote)
        }
    }
}

/**
 * Tab selector component (Today/All tabs)
 */
@Composable
private fun TabSelector(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        // Today tab (selected)
        TabItem(
            text = "Today",
            isSelected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            modifier = Modifier.weight(1f)
        )
        
        // All tab
        TabItem(
            text = "All",
            isSelected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual tab item
 */
@Composable
private fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(30.dp)
            .background(
                color = if (isSelected) {
                    DiaryColors.ButtonGradientStart
                } else {
                    DiaryColors.White
                },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(
                    topStart = if (text == "Today") 4.dp else 0.dp,
                    bottomStart = if (text == "Today") 4.dp else 0.dp,
                    topEnd = if (text == "All") 4.dp else 0.dp,
                    bottomEnd = if (text == "All") 4.dp else 0.dp
                )
            )
            .then(
                if (!isSelected) {
                    Modifier.border(
                        width = 0.5.dp,
                        color = DiaryColors.LightGrayBorder,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                            topStart = if (text == "Today") 4.dp else 0.dp,
                            bottomStart = if (text == "Today") 4.dp else 0.dp,
                            topEnd = if (text == "All") 4.dp else 0.dp,
                            bottomEnd = if (text == "All") 4.dp else 0.dp
                        )
                    )
                } else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = LoginTypography.InputFieldText.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) DiaryColors.White else DiaryColors.GrayText
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812)
@Composable
fun DiaryMainScreenPreview() {
    DiaryMainScreen()
}
