package fridger.com.io.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 定義應用程式中使用的標準間距
 */
data class Spacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,
    val extraLarge: Dp = 20.dp,
    val huge: Dp = 24.dp,
    val extraHuge: Dp = 32.dp,
    val massive: Dp = 48.dp,
    val gigantic: Dp = 60.dp,
)

/**
 * 定義應用程式中使用的標準尺寸
 */
data class Sizing(
    // Icon sizes
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 20.dp,
    val iconLarge: Dp = 24.dp,
    val iconExtraLarge: Dp = 32.dp,
    val iconHuge: Dp = 48.dp,

    // Card and component heights
    val cardHeightSmall: Dp = 80.dp,
    val cardHeightMedium: Dp = 100.dp,
    val cardHeightLarge: Dp = 120.dp,

    // Corner radius
    val cornerRadiusSmall: Dp = 4.dp,
    val cornerRadiusMedium: Dp = 8.dp,
    val cornerRadiusLarge: Dp = 12.dp,
    val cornerRadiusExtraLarge: Dp = 16.dp,

    // Progress indicators
    val progressBarHeight: Dp = 8.dp,

    // Content padding
    val contentPaddingHorizontal: Dp = 20.dp,
    val contentPaddingVertical: Dp = 20.dp,
    val contentPaddingTop: Dp = 60.dp,
)

val LocalSpacing = compositionLocalOf { Spacing() }
val LocalSizing = compositionLocalOf { Sizing() }

/**
 * 提供訪問 MaterialTheme 間距的便捷方式
 */
val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

/**
 * 提供訪問 MaterialTheme 尺寸的便捷方式
 */
val MaterialTheme.sizing: Sizing
    @Composable
    @ReadOnlyComposable
    get() = LocalSizing.current
