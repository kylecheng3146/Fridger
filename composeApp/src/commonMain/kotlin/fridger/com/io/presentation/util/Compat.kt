package fridger.com.io.presentation.util

import androidx.compose.ui.Modifier

// Multiplatform compatibility for item placement animation.
// On platforms where the API isn't available, this is a no-op.
expect fun Modifier.animateItemPlacementCompat(): Modifier
