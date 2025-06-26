package com.example.aitoolpoc.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

// Typography styles based on Figma design specifications
object LoginTypography {
    // Nunito font family (fallback to system fonts if not available)
    val NunitoFontFamily = FontFamily.Default
    val ManropeFontFamily = FontFamily.Default

    // Login title - Nunito ExtraBold 29sp
    val LoginTitle = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 29.sp,
        lineHeight = (29 * 1.364).sp,
        color = LoginColors.DarkText
    )

    // Section labels - Nunito Regular 16sp
    val SectionLabel = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = (16 * 1.25).sp,
        color = LoginColors.GrayText
    )

    // Helper text - Nunito Regular 12sp
    val HelperText = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = (12 * 1.364).sp,
        color = LoginColors.DarkText
    )

    // Forgot PIN link - Nunito Bold 14sp
    val ForgotPinLink = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = (14 * 1.364).sp,
        color = LoginColors.TealAccent,
        textAlign = TextAlign.Center
    )

    // Or divider text - Manrope Medium 14sp
    val OrDividerText = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = (14 * 1.366).sp,
        color = LoginColors.MediumGray
    )

    // Email login link - Nunito Bold 16sp
    val EmailLoginLink = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = (16 * 1.364).sp,
        color = LoginColors.TealAccent
    )

    // Next button text - Nunito Bold 16sp
    val NextButtonText = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = (16 * 1.364).sp,
        color = LoginColors.White,
        textAlign = TextAlign.Center
    )

    // Input field text - Nunito Regular 16sp
    val InputFieldText = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = LoginColors.DarkText,
        textAlign = TextAlign.Center
    )
}

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)