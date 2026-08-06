package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PremiumDarkGreenColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = DarkGreenBackground,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldOnContainer,
    secondary = EmeraldLight,
    onSecondary = DarkGreenBackground,
    secondaryContainer = EmeraldContainer,
    onSecondaryContainer = EmeraldOnContainer,
    tertiary = AlertAmber,
    error = WarningRed,
    background = DarkGreenBackground,
    onBackground = TextPrimary,
    surface = DarkGreenSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkGreenSurfaceSecondary,
    onSurfaceVariant = TextSecondary,
    outline = DarkGreenCardBorder
)

@Composable
fun DeepGuardTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PremiumDarkGreenColorScheme,
        typography = Typography,
        content = content
    )
}



