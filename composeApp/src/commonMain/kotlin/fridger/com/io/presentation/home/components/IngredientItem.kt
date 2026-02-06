package fridger.com.io.presentation.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fridger.com.io.data.model.Freshness
import fridger.com.io.presentation.home.ExpiryDisplay
import fridger.com.io.presentation.home.RefrigeratedItem
import fridger.com.io.utils.stringResourceFormat
import fridger.composeapp.generated.resources.Res
import fridger.composeapp.generated.resources.home_days_due_today
import fridger.composeapp.generated.resources.home_days_overdue
import fridger.composeapp.generated.resources.home_days_until
import fridger.composeapp.generated.resources.home_eat_it_soon
import fridger.composeapp.generated.resources.home_group_section_expired
import fridger.composeapp.generated.resources.home_group_section_fresh
import fridger.composeapp.generated.resources.home_group_section_nearing
import fridger.composeapp.generated.resources.home_remove_ingredient
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.animation.core.animateDpAsState
import org.jetbrains.compose.resources.painterResource


@Composable
fun IngredientItem(
    item: RefrigeratedItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onRemove: (() -> Unit)? = null
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        label = "borderColorAnimation"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    // Smooth scale animation for hover/press
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.98f
            isHovered -> 1.01f
            else -> 1f
        },
        label = "scale"
    )
    
    // Elevate on hover
    val elevation by animateDpAsState(
        targetValue = if (isHovered) 4.dp else 0.dp,
        label = "elevation"
    )

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large, // 16.dp
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Placeholder with soft background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        color = getFreshnessColor(item.freshness).copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(item.icon),
                    contentDescription = item.name,
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val expiryText = when (val display = item.expiryDisplay) {
                    is ExpiryDisplay.DueToday -> stringResource(Res.string.home_days_due_today)
                    is ExpiryDisplay.Overdue -> stringResourceFormat(Res.string.home_days_overdue, display.days)
                    is ExpiryDisplay.Until -> stringResourceFormat(Res.string.home_days_until, display.days)
                }

                Surface(
                    color = getFreshnessColor(item.freshness).copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = expiryText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = getFreshnessColor(item.freshness),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (item.hasWarning) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.home_eat_it_soon),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (onRemove != null && !isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.home_remove_ingredient),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun getFreshnessColor(freshness: Freshness): Color =
    when (freshness) {
        Freshness.Fresh -> MaterialTheme.colorScheme.primary
        Freshness.NearingExpiration -> MaterialTheme.colorScheme.secondary
        Freshness.Expired -> MaterialTheme.colorScheme.error
    }

@Composable
fun IngredientCompactCard(
    item: RefrigeratedItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onRemove: (() -> Unit)? = null,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        label = "compact_border"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    // Subtle scale animation
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.98f
            isHovered -> 1.01f
            else -> 1f
        },
        label = "scale"
    )
    
    // Elevate on hover
    val elevation by animateDpAsState(
        targetValue = if (isHovered) 3.dp else 0.dp,
        label = "elevation"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .pointerHoverIcon(PointerIcon.Hand),
        shape = MaterialTheme.shapes.large, // 16.dp
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(getFreshnessColor(item.freshness).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.name,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold, // Increased weight
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = item.quantity,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (onRemove != null && !isSelected) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(28.dp) // Slightly bigger target
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.home_remove_ingredient),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FreshnessBadge(freshness = item.freshness)
                val expiryText =
                    when (val display = item.expiryDisplay) {
                        is ExpiryDisplay.DueToday -> stringResource(Res.string.home_days_due_today)
                        is ExpiryDisplay.Overdue -> stringResourceFormat(Res.string.home_days_overdue, display.days)
                        is ExpiryDisplay.Until -> stringResourceFormat(Res.string.home_days_until, display.days)
                    }
                Text(
                    text = expiryText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold, // Bold for readability
                    color = getFreshnessColor(item.freshness)
                )
            }
        }
    }
}

@Composable
private fun FreshnessBadge(freshness: Freshness) {
    val (label, color) =
        when (freshness) {
            Freshness.Fresh -> Res.string.home_group_section_fresh to MaterialTheme.colorScheme.primary
            Freshness.NearingExpiration -> Res.string.home_group_section_nearing to MaterialTheme.colorScheme.secondary
            Freshness.Expired -> Res.string.home_group_section_expired to MaterialTheme.colorScheme.error
        }
    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = stringResource(label),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
// Helper to collect hover state
@Composable
fun InteractionSource.collectIsHoveredAsState(): State<Boolean> {
    val isHovered = remember { mutableStateOf(false) }
    LaunchedEffect(this) {
        interactions.collect { interaction ->
            when (interaction) {
                is HoverInteraction.Enter -> isHovered.value = true
                is HoverInteraction.Exit -> isHovered.value = false
            }
        }
    }
    return isHovered
}
