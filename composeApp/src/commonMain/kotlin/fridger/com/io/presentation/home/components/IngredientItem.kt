package fridger.com.io.presentation.home.components

import fridger.com.io.utils.stringResourceFormat
import fridger.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fridger.com.io.data.model.Freshness
import fridger.com.io.presentation.home.ExpiryDisplay
import fridger.com.io.presentation.home.RefrigeratedItem

import fridger.com.io.utils.stringResourceFormat
import fridger.composeapp.generated.resources.*
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = if (isSelected) BorderStroke(2.dp, borderColor) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Placeholder
            Box(
                modifier = Modifier
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
                val expiryText = when (val display = item.expiryDisplay) {
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
private fun getFreshnessColor(freshness: Freshness): Color {
    return when (freshness) {
        Freshness.Fresh -> Color(0xFF4CAF50) // 新鮮綠
        Freshness.NearingExpiration -> Color(0xFFFF9800) // 暖橙色
        Freshness.Expired -> Color(0xFFF44336) // 紅色
    }
}
