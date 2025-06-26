package com.example.aitoolpoc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.example.aitoolpoc.ui.theme.LoginColors
import com.example.aitoolpoc.ui.theme.LoginTypography

/**
 * Back arrow button component
 */
@Composable
fun BackArrowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 9.6.dp, height = 16.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(com.example.aitoolpoc.R.drawable.ic_back_arrow),
            contentDescription = "Back",
            tint = LoginColors.DarkText,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Single character input field with bottom border
 * Supports both numeric and alphanumeric input based on inputType
 */
@Composable
fun SingleCharacterInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onNext: (() -> Unit)? = null,
    onPrevious: (() -> Unit)? = null,
    inputType: InputType = InputType.NUMERIC
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                when {
                    newValue.isEmpty() -> {
                        // Handle deletion - move to previous field
                        onValueChange("")
                        onPrevious?.invoke()
                    }
                    newValue.length == 1 -> {
                        // Validate input based on type (no case conversion)
                        val isValid = when (inputType) {
                            InputType.NUMERIC -> newValue.all { it.isDigit() }
                            InputType.ALPHANUMERIC -> newValue.all { char ->
                                char.isDigit() || char.isLetter()
                            }
                        }

                        if (isValid) {
                            // Keep input exactly as user typed
                            onValueChange(newValue)
                            // Auto-advance to next field
                            onNext?.invoke()
                        }
                    }
                    // Ignore input longer than 1 character
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = when (inputType) {
                    InputType.NUMERIC -> KeyboardType.Number
                    InputType.ALPHANUMERIC -> KeyboardType.Ascii
                }
            ),
            textStyle = LoginTypography.InputFieldText,
            modifier = Modifier
                .width(64.dp)
                .padding(bottom = 8.dp)
                .focusRequester(focusRequester),
            singleLine = true
        )

        // Bottom border line
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(2.dp)
                .background(LoginColors.LightGrayBorder)
        )
    }
}

/**
 * Input type enum for different field types
 */
enum class InputType {
    NUMERIC,
    ALPHANUMERIC
}

/**
 * Four character input row component with auto-advance functionality
 */
@Composable
fun FourCharacterInputRow(
    values: List<String>,
    onValuesChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    inputType: InputType = InputType.NUMERIC
) {
    val focusRequesters = remember { List(4) { FocusRequester() } }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(29.dp)
    ) {
        repeat(4) { index ->
            SingleCharacterInput(
                value = values.getOrNull(index) ?: "",
                onValueChange = { newValue ->
                    val newValues = values.toMutableList()
                    while (newValues.size <= index) {
                        newValues.add("")
                    }
                    newValues[index] = newValue
                    onValuesChange(newValues)
                },
                focusRequester = focusRequesters[index],
                onNext = if (index < 3) {
                    {
                        // Move focus to next field
                        focusRequesters[index + 1].requestFocus()
                    }
                } else null,
                onPrevious = if (index > 0) {
                    {
                        // Move focus to previous field
                        focusRequesters[index - 1].requestFocus()
                    }
                } else null,
                inputType = inputType,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Section with label and four character input
 */
@Composable
fun InputSection(
    label: String,
    values: List<String>,
    onValuesChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    inputType: InputType = InputType.NUMERIC
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = label,
            style = LoginTypography.SectionLabel
        )

        FourCharacterInputRow(
            values = values,
            onValuesChange = onValuesChange,
            inputType = inputType
        )
    }
}

/**
 * Or divider component with line and text
 */
@Composable
fun OrDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(152.dp)
            .height(25.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LoginColors.LightGrayBorder)
        )

        // "or" text with background
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(25.dp)
                .background(LoginColors.White)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "or",
                style = LoginTypography.OrDividerText
            )
        }
    }
}

/**
 * Email login link button
 */
@Composable
fun EmailLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(172.dp)
            .height(22.dp)
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = LoginColors.TealAccent,
                shape = RoundedCornerShape(0.dp)
            )
            .padding(bottom = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Login through Email ID",
            style = LoginTypography.EmailLoginLink.copy(
                textDecoration = TextDecoration.Underline
            )
        )
    }
}

/**
 * Gradient next button at bottom
 */
@Composable
fun NextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(LoginColors.GradientStart, LoginColors.GradientEnd)
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Next",
                style = LoginTypography.NextButtonText
            )

            Icon(
                imageVector = ImageVector.vectorResource(com.example.aitoolpoc.R.drawable.ic_next_arrow),
                contentDescription = "Next",
                tint = LoginColors.White,
                modifier = Modifier.size(width = 7.8.dp, height = 13.dp)
            )
        }
    }
}
