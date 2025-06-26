package com.example.aitoolpoc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aitoolpoc.ui.theme.LoginColors
import com.example.aitoolpoc.ui.theme.LoginTypography
import com.example.aitoolpoc.viewmodel.AddNoteViewModel

/**
 * Add Note screen component based on Figma design
 * Screen dimensions: 375x812 (iPhone design)
 */
@Composable
fun AddNoteScreen(
    onBackPressed: () -> Unit = {},
    onNoteSaved: () -> Unit = {},
    onCancelPressed: () -> Unit = {},
    editNoteId: String? = null,  // If provided, edit mode
    editTitle: String = "",      // Pre-filled title for edit mode
    editDescription: String = "" // Pre-filled description for edit mode
) {
    val context = LocalContext.current
    val viewModel: AddNoteViewModel = viewModel { AddNoteViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    // Initialize with edit data if in edit mode
    var title by remember { mutableStateOf(editTitle) }
    var description by remember { mutableStateOf(editDescription) }
    val isEditMode = editNoteId != null
    var isLoadingNoteData by remember { mutableStateOf(false) }

    android.util.Log.d("AddNoteScreen", "Screen mode: ${if (isEditMode) "EDIT" else "CREATE"}")
    android.util.Log.d("AddNoteScreen", "Edit Note ID: $editNoteId")
    android.util.Log.d("AddNoteScreen", "Pre-filled Title: '$editTitle'")
    android.util.Log.d("AddNoteScreen", "Pre-filled Description: '$editDescription'")

    // Create a unique key for this screen instance to force state reset
    val screenKey = remember(editNoteId, editTitle, editDescription) {
        "${editNoteId ?: "new"}_${System.currentTimeMillis()}"
    }

    // Track if we've already handled the save success to prevent multiple triggers
    var hasHandledSaveSuccess by remember(screenKey) { mutableStateOf(false) }

    // Reset save state when screen is first opened or when switching between edit/create modes
    LaunchedEffect(screenKey) {
        android.util.Log.d("AddNoteScreen", "Screen opened with key: $screenKey - resetting state")
        android.util.Log.d("AddNoteScreen", "Edit mode: ${editNoteId != null}, EditId: $editNoteId")
        viewModel.resetSaveState()
        hasHandledSaveSuccess = false

        // If in edit mode but no pre-filled data, fetch the note data
        if (editNoteId != null && editTitle.isEmpty() && editDescription.isEmpty()) {
            android.util.Log.d("AddNoteScreen", "Edit mode detected but no pre-filled data - fetching note data")
            isLoadingNoteData = true
            viewModel.loadNoteForEdit(editNoteId)
        }
    }

    // Handle loaded note data for edit mode
    LaunchedEffect(uiState.loadedNoteData) {
        uiState.loadedNoteData?.let { noteData ->
            android.util.Log.d("AddNoteScreen", "Loading note data into form fields")
            android.util.Log.d("AddNoteScreen", "Loaded title: '${noteData.title}'")
            android.util.Log.d("AddNoteScreen", "Loaded description: '${noteData.description}'")
            title = noteData.title
            description = noteData.description
            isLoadingNoteData = false
        }
    }

    // Handle save success - only trigger once per save operation and only for current screen instance
    LaunchedEffect(uiState.isSaveSuccessful, screenKey) {
        if (uiState.isSaveSuccessful && !hasHandledSaveSuccess) {
            android.util.Log.d("AddNoteScreen", "Save successful for screen key: $screenKey, triggering onNoteSaved callback")
            hasHandledSaveSuccess = true
            // Add a small delay to ensure the state is properly set before triggering callback
            kotlinx.coroutines.delay(100)
            viewModel.resetSaveState()
            onNoteSaved()
        }
    }
    
    // Handle error messages
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Section
            AddNoteHeader(
                onBackPressed = onBackPressed,
                title = if (isEditMode) "Edit Note" else "Add Note",
                modifier = Modifier.fillMaxWidth()
            )
            
            // Divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFD8D8D8))
            )
            
            // Content Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title Input Section
                AddNoteInputSection(
                    label = "Title",
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Headache",
                    singleLine = true
                )
                
                // Description Input Section
                AddNoteInputSection(
                    label = "Description",
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Today after taking cancer medicine my head and neck were paining a lot.",
                    singleLine = false,
                    minHeight = 197.dp
                )
                
                // Image Upload Section
                AddNoteImageSection()
            }
        }
        
        // Bottom Action Buttons
        AddNoteActionButtons(
            onCancelPressed = onCancelPressed,
            onSavePressed = {
                if (title.isNotBlank() && description.isNotBlank()) {
                    // Pass the imageId from UI state (empty string if no image uploaded)
                    val imageId = uiState.uploadedImageId ?: ""
                    android.util.Log.d("AddNoteScreen", "${if (isEditMode) "Updating" else "Saving"} note with imageId: '$imageId'")
                    viewModel.saveNote(title, description, imageId, editNoteId)
                } else {
                    android.widget.Toast.makeText(
                        context,
                        "Please fill in both title and description",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            },
            isLoading = uiState.isLoading,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        // Loading overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00A88B))
            }
        }
    }
}

/**
 * Header component with back button and title
 */
@Composable
private fun AddNoteHeader(
    onBackPressed: () -> Unit,
    title: String = "Add Note",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(58.dp)
            .background(Color.White)
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
            // Back arrow icon (simplified)
            Text(
                text = "←",
                style = LoginTypography.InputFieldText.copy(
                    fontSize = 20.sp,
                    color = Color(0xFF1B2B3C)
                )
            )
        }
        
        // Title
        Text(
            text = title.uppercase(),
            style = LoginTypography.InputFieldText.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B2B3C),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/**
 * Input section component for title and description
 */
@Composable
private fun AddNoteInputSection(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
    minHeight: Dp = 39.dp
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Label
        Text(
            text = label,
            style = LoginTypography.InputFieldText.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF838E97)
            )
        )
        
        // Input field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight)
                .border(
                    width = 0.5.dp,
                    color = Color(0xFFCFD5DA),
                    shape = RoundedCornerShape(4.dp)
                )
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(12.dp),
            contentAlignment = Alignment.TopStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = LoginTypography.InputFieldText.copy(
                        fontSize = 14.sp,
                        color = Color(0xFF838E97),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = LoginTypography.InputFieldText.copy(
                    fontSize = 14.sp,
                    color = Color(0xFF1B2B3C),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                ),
                singleLine = singleLine,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(align = Alignment.Top)
            )
        }
    }
}

/**
 * Image upload section component
 */
@Composable
private fun AddNoteImageSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Image label
        Text(
            text = "Image (Optional)",
            style = LoginTypography.InputFieldText.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1B2B3C)
            )
        )
        
        // Image size info
        Text(
            text = "PNG or JPG, max size 1MB",
            style = LoginTypography.InputFieldText.copy(
                fontSize = 14.sp,
                color = Color(0xFF838E97)
            )
        )
        
        // Image upload area
        Box(
            modifier = Modifier
                .size(150.dp)
                .border(
                    width = 0.5.dp,
                    color = Color(0xFFD5D6D7),
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    color = Color(0xFFF0F7F7),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                    // TODO: Implement image picker
                },
            contentAlignment = Alignment.Center
        ) {
            // Image icon placeholder
            Text(
                text = "📷",
                style = LoginTypography.InputFieldText.copy(
                    fontSize = 32.sp,
                    color = Color(0xFF838E97)
                )
            )
        }
    }
}

/**
 * Bottom action buttons (Cancel/Save)
 */
@Composable
private fun AddNoteActionButtons(
    onCancelPressed: () -> Unit,
    onSavePressed: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        // Cancel button
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.White)
                .clickable { onCancelPressed() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cancel",
                style = LoginTypography.InputFieldText.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00A88B)
                )
            )
        }
        
        // Save button
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF007DB2),
                            Color(0xFF00D6AF)
                        )
                    )
                )
                .clickable {
                    if (!isLoading) onSavePressed()
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Save",
                    style = LoginTypography.InputFieldText.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}
