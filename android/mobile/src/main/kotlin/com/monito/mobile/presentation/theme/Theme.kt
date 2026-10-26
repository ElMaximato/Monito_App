package com.monito.mobile.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MonitoColors = darkColorScheme(
    primary = Teal,
    onPrimary = SpaceNight,
    secondary = Orange,
    tertiary = Pink,
    background = SpaceNight,
    surface = SpaceSurface,
    onBackground = Cream,
    onSurface = Cream,
)

@Composable
fun MonitoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MonitoColors, content = content)
}
