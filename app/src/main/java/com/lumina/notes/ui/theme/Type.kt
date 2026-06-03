package com.lumina.notes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Default = FontFamily.Default

val LuminaTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = Default, fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Default, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.25).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Default, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Default, fontWeight = FontWeight.Medium,
        fontSize = 17.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Default, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.4.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Default, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Default, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
)
