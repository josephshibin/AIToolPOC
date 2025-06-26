package com.example.aitoolpoc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aitoolpoc.data.DiaryNote
import com.example.aitoolpoc.ui.theme.DiaryColors
import com.example.aitoolpoc.ui.theme.LoginTypography
import kotlin.math.roundToInt

/**
 * Header component with gradient background and title
 */
@Composable
fun DiaryHeader(
    onBackPressed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(DiaryColors.HeaderGradientStart, DiaryColors.HeaderGradientEnd)
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back arrow
            Icon(
                imageVector = ImageVector.vectorResource(com.example.aitoolpoc.R.drawable.ic_back_arrow),
                contentDescription = "Back",
                tint = DiaryColors.White,
                modifier = Modifier
                    .size(width = 9.6.dp, height = 16.dp)
                    .clickable { onBackPressed() }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Title
            Text(
                text = "MY DIARY",
                style = LoginTypography.InputFieldText.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DiaryColors.White
                )
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Home icon placeholder
            Box(modifier = Modifier.size(24.dp))
        }
    }
}

/**
 * Date section header
 */
@Composable
fun DateSectionHeader(
    dateText: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = dateText,
        style = LoginTypography.InputFieldText.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = DiaryColors.GrayText
        ),
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * Swipeable note card component with edit and delete actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableNoteCard(
    note: DiaryNote,
    onNoteClick: (DiaryNote) -> Unit = {},
    onEditClick: (DiaryNote) -> Unit = {},
    onDeleteClick: (DiaryNote) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val swipeableState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    // Don't dismiss, just show actions
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = swipeableState,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            // Only show background when actually swiping
            if (swipeableState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 200.dp), // Push buttons to the right
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit button
                    Box(
                        modifier = Modifier
                            .width(82.dp)
                            .height(66.dp)
                            .background(
                                DiaryColors.EditActionColor,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                onEditClick(note)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Edit",
                            style = LoginTypography.InputFieldText.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DiaryColors.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Delete button
                    Box(
                        modifier = Modifier
                            .width(82.dp)
                            .height(66.dp)
                            .background(
                                DiaryColors.DeleteActionColor,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                onDeleteClick(note)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Delete",
                            style = LoginTypography.InputFieldText.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DiaryColors.White
                            )
                        )
                    }
                }
            }
        },
        content = {
            NoteCardContent(
                note = note,
                onNoteClick = onNoteClick
            )
        }
    )
}

/**
 * Alternative custom swipe implementation
 */
@Composable
fun CustomSwipeableNoteCard(
    note: DiaryNote,
    onNoteClick: (DiaryNote) -> Unit = {},
    onEditClick: (DiaryNote) -> Unit = {},
    onDeleteClick: (DiaryNote) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    val maxSwipeDistance = 164f // Width for both Edit and Delete buttons

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        // Background with action buttons
        if (offsetX < -10f) { // Only show when swiped
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top
            ) {
                // Edit button
                Box(
                    modifier = Modifier
                        .width(82.dp)
                        .height(66.dp)
                        .background(
                            DiaryColors.EditActionColor,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            onEditClick(note)
                            offsetX = 0f // Reset position
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Edit",
                        style = LoginTypography.InputFieldText.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DiaryColors.White
                        )
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Delete button
                Box(
                    modifier = Modifier
                        .width(82.dp)
                        .height(66.dp)
                        .background(
                            DiaryColors.DeleteActionColor,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            onDeleteClick(note)
                            offsetX = 0f // Reset position
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Delete",
                        style = LoginTypography.InputFieldText.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DiaryColors.White
                        )
                    )
                }
            }
        }

        // Main note card that can be swiped
        Box(
            modifier = Modifier
                .offset(x = offsetX.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // Snap to position based on swipe distance
                            offsetX = if (offsetX < -maxSwipeDistance / 2) {
                                -maxSwipeDistance
                            } else {
                                0f
                            }
                        }
                    ) { _, dragAmount ->
                        // Only allow left swipe (negative values)
                        val newOffset = offsetX + dragAmount
                        offsetX = newOffset.coerceIn(-maxSwipeDistance, 0f)
                    }
                }
        ) {
            NoteCardContent(
                note = note,
                onNoteClick = onNoteClick
            )
        }
    }
}

/**
 * Note card content (separated for reuse)
 */
@Composable
private fun NoteCardContent(
    note: DiaryNote,
    onNoteClick: (DiaryNote) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Main note card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp) // Fixed height as per Figma
                .clip(RoundedCornerShape(4.dp))
                .background(DiaryColors.NoteCardBackground)
                .border(
                    width = 0.5.dp,
                    color = DiaryColors.NoteCardBorder.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable { onNoteClick(note) }
                .padding(16.dp)
        ) {
            // Main content area - title and description
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp) // Leave space for menu icon
            ) {
                // Title - left aligned, ExtraBold 16sp
                Text(
                    text = note.title,
                    style = LoginTypography.InputFieldText.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DiaryColors.DarkText,
                        textAlign = TextAlign.Start
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Content preview - left aligned, Regular 13sp
                Text(
                    text = note.getContentPreview(),
                    style = LoginTypography.InputFieldText.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = DiaryColors.GrayText,
                        textAlign = TextAlign.Start
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // More icon in top right (3 vertical dots)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(6.dp, 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⋮",
                    style = LoginTypography.InputFieldText.copy(
                        fontSize = 16.sp,
                        color = DiaryColors.GrayText
                    )
                )
            }
        }

        // Time stamp below the card (as per Figma)
        Text(
            text = note.getFormattedTime(),
            style = LoginTypography.InputFieldText.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = DiaryColors.GrayText
            ),
            modifier = Modifier.padding(start = 0.dp, top = 4.dp)
        )
    }
}

/**
 * Add new note button
 */
@Composable
fun AddNewNoteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(DiaryColors.ButtonGradientStart, DiaryColors.ButtonGradientEnd)
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Add New Note",
            style = LoginTypography.InputFieldText.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DiaryColors.White
            )
        )
    }
}
