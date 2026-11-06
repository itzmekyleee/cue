package ph.edu.comteq.cue.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CueBlue,
    onPrimary = Color.White,
    primaryContainer = LightAccentContainer,
    onPrimaryContainer = LightOnAccentContainer,
    inversePrimary = Color(0xFF66B0FF),
    secondary = CueBlue,
    onSecondary = Color.White,
    secondaryContainer = LightAccentContainer,
    onSecondaryContainer = LightOnAccentContainer,
    tertiary = CueBlue,
    onTertiary = Color.White,
    tertiaryContainer = LightAccentContainer,
    onTertiaryContainer = LightOnAccentContainer,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = CueBlue,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightBackground,
    surfaceContainerHigh = LightSurfaceVariant,
    surfaceContainerHighest = Color(0xFFD1D1D6),
    outline = LightOutline,
    outlineVariant = LightSurfaceVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = CueBlueDark,
    onPrimary = Color.White,
    primaryContainer = DarkAccentContainer,
    onPrimaryContainer = DarkOnAccentContainer,
    inversePrimary = CueBlue,
    secondary = CueBlueDark,
    onSecondary = Color.White,
    secondaryContainer = DarkAccentContainer,
    onSecondaryContainer = DarkOnAccentContainer,
    tertiary = CueBlueDark,
    onTertiary = Color.White,
    tertiaryContainer = DarkAccentContainer,
    onTertiaryContainer = DarkOnAccentContainer,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = CueBlueDark,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = DarkOutline,
    outline = DarkOutline,
    outlineVariant = DarkSurfaceVariant
)

/**
 * Cue's single-accent flat theme. Dynamic color is intentionally disabled so
 * the app keeps its one blue accent and rounded, iOS-style surfaces.
 */
@Composable
fun CueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = CueShapes,
        content = content
    )
}
