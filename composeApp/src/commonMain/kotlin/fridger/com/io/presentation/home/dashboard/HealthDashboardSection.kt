package fridger.com.io.presentation.home.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fridger.com.io.presentation.home.HealthDashboardUiState
import fridger.com.io.ui.theme.AppColors
import fridger.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import fridger.shared.health.HealthDashboardMetrics
import fridger.shared.health.NutritionCategory
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

@Composable
fun HealthDashboardBentoGrid(
    state: HealthDashboardUiState,
    onRefresh: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        when {
            state.isLoading -> {
                DashboardSkeleton(modifier = Modifier.fillMaxWidth())
            }
            state.error != null -> {
                ErrorState(error = state.error!!, onRetry = onRefresh)
            }
            state.metrics != null -> {
                BentoGridContent(metrics = state.metrics!!, onViewDetails = onViewDetails)
            }
            else -> {
                Text(stringResource(Res.string.dashboard_no_data), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDashboardDetailSheet(
    state: HealthDashboardUiState,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.dashboard_detail_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                state.error != null -> Text(state.error!!, color = AppColors.Error)
                state.metrics != null -> {
                    val metrics = state.metrics!!
                    // Detailed List View
                    DetailSection(title = stringResource(Res.string.dashboard_nutrition_breakdown)) {
                        NutritionDistributionWidget(metrics.nutritionDistribution)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    DetailSection(title = stringResource(Res.string.dashboard_recommendations)) {
                        metrics.recommendations.forEach {
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AppColors.Success, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(it.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(Res.string.dashboard_error_title), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text(stringResource(Res.string.dashboard_retry))
            }
        }
    }
}

@Composable
private fun BentoGridContent(metrics: HealthDashboardMetrics, onViewDetails: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Row 1: Nutrition Distribution (Large) & Diversity (Medium)
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BentoCard(
                modifier = Modifier.weight(1.5f).fillMaxHeight(),
                title = stringResource(Res.string.dashboard_widget_nutrition_balance),
                icon = Icons.Default.PieChart,
                color = MaterialTheme.colorScheme.primary
            ) {
                NutritionDistributionWidget(metrics.nutritionDistribution)
            }
            
            BentoCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                title = stringResource(Res.string.dashboard_widget_diversity),
                icon = Icons.Default.ColorLens,
                color = diversityStatusColor(metrics.diversityScore.value)
            ) {
                DiversityScoreWidget(
                    score = metrics.diversityScore.value, 
                    rating = metrics.diversityScore.rating.name,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2: Expiry Alerts & Recommendations
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BentoCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                title = stringResource(Res.string.dashboard_widget_expiring_soon),
                icon = Icons.Default.Warning,
                color = if (metrics.expiryAlerts.isNotEmpty()) AppColors.Warning else AppColors.Success,
                onClick = onViewDetails
            ) {
                ExpiryAlertWidget(
                    count = metrics.expiryAlerts.size,
                    modifier = Modifier.weight(1f)
                )
            }

            BentoCard(
                modifier = Modifier.weight(1.5f).fillMaxHeight(),
                title = stringResource(Res.string.dashboard_widget_smart_tips),
                icon = Icons.Default.Lightbulb,
                color = MaterialTheme.colorScheme.secondary
            ) {
                val tip = metrics.recommendations.firstOrNull()?.message ?: stringResource(Res.string.dashboard_default_tip)
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
private fun BentoCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick).pointerHoverIcon(PointerIcon.Hand) else Modifier
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.1f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}

@Composable
private fun NutritionDistributionWidget(distribution: Map<NutritionCategory, Double>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val sorted = distribution.entries.sortedByDescending { it.value }.take(3)
        sorted.forEachIndexed { index, (category, value) ->
            key(category) {
                NutritionItem(category, value, index)
            }
        }
    }
}

@Composable
private fun NutritionItem(category: NutritionCategory, value: Double, index: Int) {
    val progressAnim = remember { Animatable(0f) }
    
    LaunchedEffect(value) {
        progressAnim.snapTo(0f)
        progressAnim.animateTo(
            targetValue = value.toFloat() / 100f,
            animationSpec = tween(durationMillis = 1000, delayMillis = index * 150, easing = FastOutSlowInEasing)
        )
    }

    val percentage = (progressAnim.value * 100).roundToInt()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    LinearProgressIndicator(
        progress = { progressAnim.value },
        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
        color = nutritionStatusColor(value),
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Composable
private fun DiversityScoreWidget(
    score: Int,
    rating: String,
    modifier: Modifier = Modifier
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxWidth()) {
        var lastAnimatedScore by rememberSaveable { mutableStateOf<Int?>(null) }

        val targetProgress = score / 100f
        val progressAnim = remember { Animatable(if (lastAnimatedScore == score) targetProgress else 0f) }
        val scoreAnim = remember { Animatable(if (lastAnimatedScore == score) score.toFloat() else 0f) }

        LaunchedEffect(score) {
            if (lastAnimatedScore != score) {
                progressAnim.snapTo(0f)
                scoreAnim.snapTo(0f)

                progressAnim.animateTo(
                    targetValue = targetProgress,
                    animationSpec = tween(durationMillis = 1500, delayMillis = 200, easing = FastOutSlowInEasing)
                )
            } else {
                progressAnim.snapTo(targetProgress)
            }
        }
        LaunchedEffect(score) {
            if (lastAnimatedScore != score) {
                 scoreAnim.animateTo(
                    targetValue = score.toFloat(),
                    animationSpec = tween(durationMillis = 1500, delayMillis = 200, easing = FastOutSlowInEasing)
                )
                lastAnimatedScore = score
            } else {
                scoreAnim.snapTo(score.toFloat())
            }
        }

        CircularProgressIndicator(
            progress = { progressAnim.value },
            modifier = Modifier.size(64.dp),
            color = diversityStatusColor(score),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 8.dp
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${scoreAnim.value.roundToInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ExpiryAlertWidget(count: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var lastAnimatedCount by rememberSaveable { mutableStateOf<Int?>(null) }
        val countAnim = remember { Animatable(if (lastAnimatedCount == count) count.toFloat() else 0f) }

        LaunchedEffect(count) {
            if (lastAnimatedCount != count) {
                countAnim.snapTo(0f)
                countAnim.animateTo(
                    targetValue = count.toFloat(),
                    animationSpec = tween(durationMillis = 1000, delayMillis = 300, easing = FastOutSlowInEasing)
                )
                lastAnimatedCount = count
            } else {
                countAnim.snapTo(count.toFloat())
            }
        }

        Text(
            text = "${countAnim.value.roundToInt()}",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = expiryStatusColor(count)
        )
        Text(
            text = stringResource(Res.string.dashboard_widget_items_suffix),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

// Helper functions for colors
private fun nutritionStatusColor(percent: Double): Color =
    when {
        percent >= 40 -> AppColors.Success
        percent >= 25 -> AppColors.Warning
        else -> AppColors.Error
    }

private fun diversityStatusColor(score: Int): Color =
    when {
        score >= 70 -> AppColors.Success
        score >= 50 -> AppColors.Warning
        else -> AppColors.Error
    }

private fun expiryStatusColor(count: Int): Color =
    when {
        count == 0 -> AppColors.Success
        count <= 2 -> AppColors.Warning
        else -> AppColors.Error
    }

private fun formatTimestamp(epochMillis: Long): String {
    val dateTime = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "${dateTime.monthNumber}/${dateTime.dayOfMonth} $hour:$minute"
}

// Nutrition Category extension if not present in common
val NutritionCategory.displayName: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }

@Composable
private fun DashboardSkeleton(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonCard(modifier = Modifier.weight(1.5f).fillMaxHeight(), brush = brush)
            SkeletonCard(modifier = Modifier.weight(1f).fillMaxHeight(), brush = brush)
        }
        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonCard(modifier = Modifier.weight(1f).fillMaxHeight(), brush = brush)
            SkeletonCard(modifier = Modifier.weight(1.5f).fillMaxHeight(), brush = brush)
        }
    }
}

@Composable
private fun SkeletonCard(modifier: Modifier, brush: Brush) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
    )
}
