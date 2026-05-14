package com.example.nammaplatform.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NammaColorScheme = lightColorScheme(
    primary = NammaBlue,
    onPrimary = NammaYellow,
    secondary = NammaYellow,
    onSecondary = NammaBlue,
    background = NammaWhite,
    onBackground = NammaBlack,
    surface = NammaWhite,
    onSurface = NammaBlack
)

@Composable
fun HappyBirthdayTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NammaColorScheme,
        typography = Typography,
        content = content
    )
}