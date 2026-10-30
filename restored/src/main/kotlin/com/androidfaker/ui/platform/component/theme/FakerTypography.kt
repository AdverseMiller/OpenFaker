package com.androidfaker.ui.platform.component.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.androidfaker.restored.R

private val regular = FontFamily(Font(R.font.dm_sans_regular))
private val medium = FontFamily(Font(R.font.dm_sans_medium))
private val semiBold = FontFamily(Font(R.font.dm_sans_semi_bold))

private fun style(size: Int, height: Int, family: FontFamily, weight: FontWeight) = TextStyle(
    fontFamily = family, fontWeight = weight, fontSize = size.sp, lineHeight = height.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
)

/** Original DM Sans resources and size/line-height pairs from the recovered typography initializer. */
val FakerTypography = Typography(
    displayLarge = style(56, 64, semiBold, FontWeight.SemiBold),
    displayMedium = style(44, 52, semiBold, FontWeight.SemiBold),
    displaySmall = style(36, 44, semiBold, FontWeight.SemiBold),
    headlineLarge = style(32, 40, semiBold, FontWeight.SemiBold),
    headlineMedium = style(28, 36, semiBold, FontWeight.SemiBold),
    headlineSmall = style(18, 22, semiBold, FontWeight.SemiBold),
    titleLarge = style(19, 28, semiBold, FontWeight.SemiBold),
    titleMedium = style(16, 20, semiBold, FontWeight.SemiBold),
    titleSmall = style(14, 18, medium, FontWeight.SemiBold),
    bodyLarge = style(15, 20, regular, FontWeight.Normal),
    bodyMedium = style(13, 18, regular, FontWeight.Normal),
    bodySmall = style(12, 16, regular, FontWeight.Normal),
    labelLarge = style(14, 20, semiBold, FontWeight.SemiBold),
    labelMedium = style(12, 16, semiBold, FontWeight.SemiBold),
    labelSmall = style(12, 16, regular, FontWeight.Normal),
)
