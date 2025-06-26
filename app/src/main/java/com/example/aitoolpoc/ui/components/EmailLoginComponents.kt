package com.example.aitoolpoc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aitoolpoc.ui.theme.LoginColors
import com.example.aitoolpoc.ui.theme.LoginTypography

/**
 * Email input field with bottom border
 */
@Composable
fun EmailInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = ""
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle = LoginTypography.InputFieldText.copy(
                textAlign = androidx.compose.ui.text.style.TextAlign.Start
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = LoginTypography.InputFieldText.copy(
                                color = LoginColors.GrayText,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )

        // Bottom border line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(LoginColors.LightGrayBorder)
        )
    }
}

/**
 * Email display when email is entered and valid
 * Clickable to allow editing
 */
@Composable
fun EmailDisplay(
    email: String,
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
    ) {
        Text(
            text = email,
            style = LoginTypography.InputFieldText.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Bottom border line (darker to indicate it's filled)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(LoginColors.DarkText)
        )
    }
}

/**
 * Email input section with label
 * Shows email text when valid, input field when not
 */
@Composable
fun EmailInputSection(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    showAsText: Boolean = false,
    onEditEmail: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Only show label when not in text display mode
        if (!showAsText) {
            Text(
                text = label,
                style = LoginTypography.SectionLabel
            )
        }

        if (showAsText && value.isNotEmpty()) {
            EmailDisplay(
                email = value,
                onEditClick = onEditEmail
            )
        } else {
            EmailInputField(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder
            )
        }
    }
}

/**
 * PIN dots display for when PIN is entered
 * Clickable to allow editing
 */
@Composable
fun PinDotsDisplay(
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable { onEditClick() },
        horizontalArrangement = Arrangement.spacedBy(80.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .background(
                        color = LoginColors.DarkText,
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * PIN section with 4-digit input and helper text
 * Shows dots when PIN is complete, input fields when not
 */
@Composable
fun PinSectionWithHelper(
    pinValues: List<String>,
    onPinValuesChange: (List<String>) -> Unit,
    onForgotPinPressed: () -> Unit,
    showAsDots: Boolean = false,
    onEditPin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // PIN input section - either dots or input fields
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (showAsDots) {
                // Show dots when PIN is entered
                PinDotsDisplay(
                    onEditClick = onEditPin,
                    modifier = Modifier.height(20.dp)
                )
            } else {
                // Show input fields when PIN is not complete
                InputSection(
                    label = "Enter Pin",
                    values = pinValues,
                    onValuesChange = onPinValuesChange,
                    inputType = InputType.NUMERIC
                )
            }
        }

        // Helper text and Forgot PIN link (only show when not in dots mode)
        if (!showAsDots) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enter the PIN you have set while sign up.",
                    style = LoginTypography.HelperText,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Forgot PIN?",
                    style = LoginTypography.ForgotPinLink,
                    modifier = Modifier.clickable { onForgotPinPressed() }
                )
            }
        }
    }
}
