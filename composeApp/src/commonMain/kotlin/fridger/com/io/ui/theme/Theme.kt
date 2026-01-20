package fridger.com.io.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import fridger.com.io.presentation.settings.ThemeColor

// App Color Palette
object AppColors {
    // Primary Colors (will be replaced by dynamic theme)
    val Primary = Color(0xFF2196F3)
    val PrimaryVariant = Color(0xFF1976D2)
    val Secondary = Color(0xFFFFC107)
    val SecondaryVariant = Color(0xFFFFA000)

    // Background Colors
    val Background = Color(0xFFF5F5F5)
    val Surface = Color(0xFFFFFFFF)
    val Error = Color(0xFFF44336)

    // Text Colors
    val OnPrimary = Color(0xFFFFFFFF)
    val OnSecondary = Color(0xFF000000)
    val OnBackground = Color(0xFF000000)
    val OnSurface = Color(0xFF000000)
    val OnError = Color(0xFFFFFFFF)

    // App Specific Colors
    val TextPrimary = Color(0xFF212121)
    val TextSecondary = Color(0xFF757575)
    val Divider = Color(0xFFBDBDBD)
    val IconBackground = Color(0xFFF5F5F5)
    val Warning = Color(0xFFFFA726)
    val ProgressTrack = Color(0xFFE0E0E0)

    // Dark Theme Colors
    val DarkBackground = Color(0xFF121212)
    val DarkSurface = Color(0xFF1E1E1E)
    val DarkTextPrimary = Color(0xFFE0E0E0)
    val DarkTextSecondary = Color(0xFFBDBDBD)
    val DarkProgressTrack = Color(0xFF424242)
}

private fun getLightColorScheme(themeColor: ThemeColor): ColorScheme {
    val isTeal = themeColor == ThemeColor.TEAL
    return lightColorScheme(
        primary = themeColor.primary,
        onPrimary = AppColors.OnPrimary,
        primaryContainer = themeColor.primaryLight.copy(alpha = 0.3f),
        onPrimaryContainer = themeColor.primaryDark,
        secondary = themeColor.secondary,
        onSecondary = AppColors.OnSecondary,
        secondaryContainer = if (isTeal) themeColor.secondary.copy(alpha = 0.2f) else Color(0xFFFFE082),
        onSecondaryContainer = if (isTeal) Color(0xFF00201A) else Color(0xFF6D4C00),
        tertiary = Color(0xFF4CAF50),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFC8E6C9),
        onTertiaryContainer = Color(0xFF1B5E20),
        error = AppColors.Error,
        onError = AppColors.OnError,
        errorContainer = Color(0xFFFFCDD2),
        onErrorContainer = Color(0xFFB71C1C),
        background = AppColors.Background,
        onBackground = AppColors.OnBackground,
        surface = AppColors.Surface,
        onSurface = AppColors.OnSurface,
        surfaceVariant = Color(0xFFE0E0E0),
        onSurfaceVariant = Color(0xFF616161),
        outline = Color(0xFFBDBDBD),
        outlineVariant = Color(0xFFE0E0E0),
        scrim = Color.Black
    )
}

private fun getDarkColorScheme(themeColor: ThemeColor): ColorScheme {
    val isTeal = themeColor == ThemeColor.TEAL
    return darkColorScheme(
        primary = themeColor.primaryLight,
        onPrimary = themeColor.primaryDark,
        primaryContainer = themeColor.primaryDark,
        onPrimaryContainer = themeColor.primaryLight.copy(alpha = 0.9f),
        secondary = themeColor.secondary,
        onSecondary = if (isTeal) Color(0xFF003730) else Color(0xFF3E2D00),
        secondaryContainer = if (isTeal) themeColor.secondary.copy(alpha = 0.3f) else Color(0xFF6D4C00),
        onSecondaryContainer = if (isTeal) Color(0xFFB1ECE3) else Color(0xFFFFE082),
        tertiary = Color(0xFF81C784),
        onTertiary = Color(0xFF003907),
        tertiaryContainer = Color(0xFF1B5E20),
        onTertiaryContainer = Color(0xFFC8E6C9),
        error = Color(0xFFEF5350),
        onError = Color(0xFF5F0A0A),
        errorContainer = Color(0xFFB71C1C),
        onErrorContainer = Color(0xFFFFCDD2),
        background = AppColors.DarkBackground,
        onBackground = AppColors.DarkTextPrimary,
        surface = AppColors.DarkSurface,
        onSurface = AppColors.DarkTextPrimary,
        surfaceVariant = Color(0xFF424242),
        onSurfaceVariant = AppColors.DarkTextSecondary,
        outline = Color(0xFF757575),
        outlineVariant = Color(0xFF424242),
        scrim = Color.Black
    )
}

@Composable
fun FridgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: ThemeColor = ThemeColor.BLUE,
    content: @Composable () -> Unit
) {
    val colorScheme =
        when {
            darkTheme -> getDarkColorScheme(themeColor)
            else -> getLightColorScheme(themeColor)
        }

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalSizing provides Sizing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = content
        )
    }
}
