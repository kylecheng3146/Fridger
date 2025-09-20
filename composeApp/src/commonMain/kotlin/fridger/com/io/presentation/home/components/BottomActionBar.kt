package fridger.com.io.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fridger.composeapp.generated.resources.Res
import fridger.composeapp.generated.resources.home_selection_cancel
import fridger.composeapp.generated.resources.home_selection_generate_recipe
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomActionBar(
    selectedCount: Int,
    onCancelClick: () -> Unit,
    onGenerateRecipeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cancel button
                OutlinedButton(
                    onClick = onCancelClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(Res.string.home_selection_cancel),
                        fontSize = 16.sp
                    )
                }

                val buttonContainerColor by animateColorAsState(
                    targetValue = if (selectedCount > 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    label = "button_container_color"
                )

                val buttonContentColor by animateColorAsState(
                    targetValue = if (selectedCount > 0)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "button_content_color"
                )

                Button(
                    onClick = onGenerateRecipeClick,
                    enabled = selectedCount > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonContainerColor,
                        contentColor = buttonContentColor
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(Res.string.home_selection_generate_recipe),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
