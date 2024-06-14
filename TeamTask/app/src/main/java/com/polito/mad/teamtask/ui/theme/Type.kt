package com.polito.mad.teamtask.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.polito.mad.teamtask.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)


val Mulish = FontFamily(
    Font(resId = R.font.mulish_black, weight = FontWeight.Black),
    Font(resId = R.font.mulish_blackitalic, weight = FontWeight.Black, style = FontStyle.Italic),
    Font(resId = R.font.mulish_bold, weight = FontWeight.Bold),
    Font(resId = R.font.mulish_bolditalic, weight = FontWeight.Bold, style = FontStyle.Italic),
    Font(resId = R.font.mulish_extrabold, weight = FontWeight.ExtraBold),
    Font(resId = R.font.mulish_extrabolditalic, weight = FontWeight.ExtraBold, style = FontStyle.Italic),
    Font(resId = R.font.mulish_extralight, weight = FontWeight.ExtraLight),
    Font(resId = R.font.mulish_extralightitalic, weight = FontWeight.Bold, style = FontStyle.Italic),
    Font(resId = R.font.mulish_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(resId = R.font.mulish_light, weight = FontWeight.Light),
    Font(resId = R.font.mulish_lightitalic, weight = FontWeight.Light, style = FontStyle.Italic),
    Font(resId = R.font.mulish_medium, weight = FontWeight.Medium),
    Font(resId = R.font.mulish_mediumitalic, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(resId = R.font.mulish_regular, weight = FontWeight.Normal),
    Font(resId = R.font.mulish_semibold, weight = FontWeight.SemiBold),
    Font(resId = R.font.mulish_semibolditalic, weight = FontWeight.SemiBold, style = FontStyle.Italic)
)


val TeamTaskTypography = Typography(
    displayLarge = TextStyle(),

    displayMedium = TextStyle(),

    displaySmall = TextStyle(),

    headlineLarge = TextStyle(
        fontFamily = Mulish,
        fontWeight = FontWeight.Bold,
        fontSize = 55.sp,
        lineHeight = 60.sp,
        letterSpacing = 0.sp,
        color = Jet
    ),

    headlineMedium = TextStyle(),

    headlineSmall = TextStyle(
        fontFamily = Mulish,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        color = Jet
    ),

    titleLarge = TextStyle(
        fontFamily = Mulish,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        color = CaribbeanCurrent
    ),
    
    titleMedium = TextStyle(
        fontFamily = Mulish,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp,
        color = CaribbeanCurrent
    ),

    titleSmall = TextStyle(
        fontFamily = Mulish,
        fontWeight = FontWeight.Light,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.sp,
        color = CaribbeanCurrent
    ),

    bodyLarge = TextStyle(
        fontFamily = Mulish,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 35.sp,
        letterSpacing = 0.sp,
        color = Jet
    ),

    bodyMedium = TextStyle(
        fontFamily = Mulish,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp,
        color = Jet
    ),

    bodySmall = TextStyle(
        fontFamily = Mulish,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        color = Jet
    ),

    labelLarge = TextStyle(),

    labelMedium = TextStyle(
        fontFamily = Mulish,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = Jet
    ),

    labelSmall = TextStyle(
        fontFamily = Mulish,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.sp,
        color = CaribbeanCurrent
    )
)
