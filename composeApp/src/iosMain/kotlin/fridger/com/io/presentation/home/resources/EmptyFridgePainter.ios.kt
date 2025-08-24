package fridger.com.io.presentation.home.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import fridger.composeapp.generated.resources.Res
import fridger.composeapp.generated.resources.empty_fridge
import org.jetbrains.compose.resources.painterResource

@Composable
actual fun emptyFridgePainter(): Painter = painterResource(Res.drawable.empty_fridge)
