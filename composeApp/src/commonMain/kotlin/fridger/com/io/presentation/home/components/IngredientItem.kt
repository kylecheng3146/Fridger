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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun IngredientItem(
    item: RefrigeratedItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onRemove: (() -> Unit)? = null
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF4CAF50) else Color.Transparent,
        label = "borderColorAnimation"
    )

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = Color.White
            ),
        border = if (isSelected) BorderStroke(2.dp, borderColor) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Placeholder
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .background(
                            color = getFreshnessColor(item.freshness).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                contentAlignment = Alignment.Center
            ) {
                Text(item.icon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2D5A27) // 森林綠
                )
                Text(
                    text = item.category.name,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val expiryText =
                    when (val display = item.expiryDisplay) {
                        is ExpiryDisplay.DueToday -> stringResource(Res.string.home_days_due_today)
                        is ExpiryDisplay.Overdue -> stringResourceFormat(Res.string.home_days_overdue, display.days)
                        is ExpiryDisplay.Until -> stringResourceFormat(Res.string.home_days_until, display.days)
                    }

                Text(
                    text = expiryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = getFreshnessColor(item.freshness)
                )

                if (item.hasWarning) {
                    Text(
                        text = stringResource(Res.string.home_eat_it_soon),
                        fontSize = 10.sp,
                        color = Color(0xFFFF9800) // 暖橙色
                    )
                }
            }

            if (onRemove != null && !isSelected) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.home_remove_ingredient),
                        tint = Color.Gray.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun getFreshnessColor(freshness: Freshness): Color =
    when (freshness) {
        Freshness.Fresh -> Color(0xFF4CAF50) // 新鮮綠
        Freshness.NearingExpiration -> Color(0xFFFF9800) // 暖橙色
        Freshness.Expired -> Color(0xFFF44336) // 紅色
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
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        label = "compact_border"
    )
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(getFreshnessColor(item.freshness).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = item.icon, fontSize = 22.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = item.quantity,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (onRemove != null && !isSelected) {
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.home_remove_ingredient),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
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
            Freshness.Fresh -> Res.string.home_group_section_fresh to Color(0xFF4CAF50)
            Freshness.NearingExpiration -> Res.string.home_group_section_nearing to Color(0xFFFF9800)
            Freshness.Expired -> Res.string.home_group_section_expired to Color(0xFFF44336)
        }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = stringResource(label),
            color = color,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
