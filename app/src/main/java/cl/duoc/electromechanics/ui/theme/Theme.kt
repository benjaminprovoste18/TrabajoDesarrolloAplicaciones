package cl.duoc.electromechanics.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5E3FF),
    onPrimaryContainer = Color(0xFF001B3D),
    secondary = Amber,
    onSecondary = Color(0xFF231600),
    secondaryContainer = Color(0xFFFFE08B),
    onSecondaryContainer = Color(0xFF261A00),
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = Color(0xFFF4F6FA),
    onBackground = Color(0xFF181C22),
    surface = Color.White,
    onSurface = Color(0xFF181C22),
    surfaceVariant = Color(0xFFE1E6EF),
    onSurfaceVariant = Color(0xFF424852),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFF737983)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8C8FF),
    onPrimary = Color(0xFF00305F),
    primaryContainer = NavyLight,
    onPrimaryContainer = Color(0xFFD5E3FF),
    secondary = Amber,
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = AmberDark,
    onSecondaryContainer = Color(0xFFFFE08B),
    tertiary = Color(0xFF7DDC9A),
    background = Color(0xFF0E1218),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFE1E2E8),
    surfaceVariant = Color(0xFF2A313B),
    onSurfaceVariant = Color(0xFFC1C7D2)
)

@Composable
fun EMTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
