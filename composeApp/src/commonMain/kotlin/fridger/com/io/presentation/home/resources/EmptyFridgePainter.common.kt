package fridger.com.io.presentation.home.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

// Provide a platform-specific painter for the empty fridge illustration.
@Composable
expect fun emptyFridgePainter(): Painter
