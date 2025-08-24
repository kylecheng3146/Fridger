package fridger.com.io.presentation.home.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import fridger.com.io.R

@Composable
actual fun emptyFridgePainter(): Painter = painterResource(R.drawable.empty_fridge_android)
