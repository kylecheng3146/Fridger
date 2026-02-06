package fridger.com.io.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import fridger.com.io.presentation.settings.ThemeColor

// App Color Palette
// App Color Palette - Minimal Design System (Emerald/Amber)
object AppColors {
    // Primary (Emerald) - Freshness & Nature
    val Primary = Color(0xFF10B981) // Emerald 500
    val PrimaryContainer = Color(0xFFD1FAE5) // Emerald 100
    val OnPrimary = Color(0xFFFFFFFF)
    val OnPrimaryContainer = Color(0xFF065F46) // Emerald 800

    // Secondary (Amber) - Food & Warmth
    val Secondary = Color(0xFFF59E0B) // Amber 500
    val SecondaryContainer = Color(0xFFFEF3C7) // Amber 100
    val OnSecondary = Color(0xFFFFFFFF)
    val OnSecondaryContainer = Color(0xFF78350F) // Amber 900 (High contrast)

    // Functional & Semantic Colors
    val Success = Color(0xFF22C55E) // Green 500
    val SuccessContainer = Color(0xFFDCFCE7) // Green 100
    val Warning = Color(0xFFF59E0B) // Amber 500
    val WarningContainer = Color(0xFFFEF3C7) // Amber 100
    val Error = Color(0xFFEF4444) // Red 500
    val ErrorContainer = Color(0xFFFEE2E2) // Red 100
    val OnError = Color(0xFFFFFFFF)

    // Backgrounds & Surfaces (Light)
    val LightBackground = Color(0xFFFAFAFA) // Zinc 50
    val LightSurface = Color(0xFFFFFFFF) // White
    val LightSurfaceVariant = Color(0xFFF4F4F5) // Zinc 100
    val LightOutline = Color(0xFFE4E4E7) // Zinc 200

    // Backgrounds & Surfaces (Dark) - Elevated & Rich
    val DarkBackground = Color(0xFF0F172A) // Slate 900
    val DarkSurface = Color(0xFF1E293B) // Slate 800
    val DarkSurfaceElevated = Color(0xFF334155) // Slate 700
    val DarkOutline = Color(0xFF475569) // Slate 600
    
    // Text Colors
    val OnBackground = Color(0xFF18181B) // Zinc 900
    val OnSurface = Color(0xFF18181B) // Zinc 900
    val OnSurfaceVariant = Color(0xFF71717A) // Zinc 500
    
    val DarkOnBackground = Color(0xFFF1F5F9) // Slate 100
    val DarkOnSurface = Color(0xFFF1F5F9) // Slate 100
    val DarkOnSurfaceVariant = Color(0xFF94A3B8) // Slate 400
}

private fun getLightColorScheme(themeColor: ThemeColor): ColorScheme {
    // We prioritize the Minimal Emerald theme, but mapped somewhat if needed. 
    // For now, enforcing the new standard for consistency as per plan.
    return lightColorScheme(
        primary = AppColors.Primary,
        onPrimary = AppColors.OnPrimary,
        primaryContainer = AppColors.PrimaryContainer,
        onPrimaryContainer = AppColors.OnPrimaryContainer,
        secondary = AppColors.Secondary,
        onSecondary = AppColors.OnSecondary,
        secondaryContainer = AppColors.SecondaryContainer,
        onSecondaryContainer = AppColors.OnSecondaryContainer,
        tertiary = Color(0xFF14B8A6), // Teal 500 as accent
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFCCFBF1),
        onTertiaryContainer = Color(0xFF0F766E),
        error = AppColors.Error,
        onError = AppColors.OnError,
        errorContainer = AppColors.ErrorContainer,
        onErrorContainer = Color(0xFF991B1B),
        background = AppColors.LightBackground,
        onBackground = AppColors.OnBackground,
        surface = AppColors.LightSurface,
        onSurface = AppColors.OnSurface,
        surfaceVariant = AppColors.LightSurfaceVariant,
        onSurfaceVariant = AppColors.OnSurfaceVariant,
        outline = AppColors.LightOutline,
        outlineVariant = Color(0xFFD4D4D8),
        scrim = Color.Black.copy(alpha = 0.32f)
    )
}

private fun getDarkColorScheme(themeColor: ThemeColor): ColorScheme {
    return darkColorScheme(
        primary = AppColors.Primary,
        onPrimary = AppColors.OnPrimary,
        primaryContainer = AppColors.OnPrimaryContainer, // Consistent container usage
        onPrimaryContainer = AppColors.PrimaryContainer,
        secondary = AppColors.Secondary,
        onSecondary = AppColors.OnSecondary,
        secondaryContainer = AppColors.SecondaryContainer,
        onSecondaryContainer = AppColors.OnSecondaryContainer,
        tertiary = Color(0xFF2DD4BF),
        onTertiary = Color(0xFF003833),
        tertiaryContainer = Color(0xFF0F766E),
        onTertiaryContainer = Color(0xFFCCFBF1),
        error = AppColors.Error,
        onError = Color(0xFF450A0A),
        errorContainer = Color(0xFF991B1B),
        onErrorContainer = AppColors.ErrorContainer,
        background = AppColors.DarkBackground,
        onBackground = AppColors.DarkOnBackground,
        surface = AppColors.DarkSurface,
        onSurface = AppColors.DarkOnSurface,
        surfaceVariant = AppColors.DarkSurfaceElevated, // Use elevated surface for variant
        onSurfaceVariant = AppColors.DarkOnSurfaceVariant,
        outline = AppColors.DarkOutline,
        outlineVariant = Color(0xFF64748B),
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
            shapes = AppShapes,
            typography = Typography(),
            content = content
        )
    }
}
