package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GardenColorScheme = lightColorScheme(
    primary = MeadowGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCFCE7),
    onPrimaryContainer = MeadowGreenDark,
    secondary = SunnyYellow,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = BlossomPink,
    onTertiary = Color.White,
    background = GardenBackground,
    onBackground = TextPrimaryDark,
    surface = GardenSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = GardenSurfaceWarm,
    onSurfaceVariant = TextSecondaryDark,
    outline = GardenBorder,
    outlineVariant = GardenBorderWarm
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GardenColorScheme,
        typography = Typography,
        content = content
    )
}
