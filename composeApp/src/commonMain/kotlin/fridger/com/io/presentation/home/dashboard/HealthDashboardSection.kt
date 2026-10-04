package fridger.com.io.presentation.home.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import fridger.com.io.presentation.home.dashboard.DashboardSection
import fridger.com.io.ui.theme.AppColors
import fridger.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import fridger.shared.health.HealthDashboardMetrics
import fridger.shared.health.NutritionCategory
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
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
    onAddIngredient: () -> Unit,
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
                BentoGridContent(metrics = state.metrics!!, onViewDetails = onViewDetails, onAddIngredient = onAddIngredient)
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
    onRecommendationAction: (fridger.shared.health.HealthRecommendation) -> Unit,
    onAddIngredient: () -> Unit,
    onSectionToggle: (DashboardSection, Boolean) -> Unit,
    onCollapsedImpression: (DashboardSection, Boolean, Long) -> Unit,
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
                .heightIn(max = 720.dp)
                .verticalScroll(rememberScrollState())
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
                    Text(
                        "這裡統計冰箱庫存品項，不代表實際飲食；比例按品項筆數計算。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    DashboardFoldSection(
                        section = DashboardSection.INDICATORS,
                        title = "庫存品項組成",
                        expanded = state.sectionStates[DashboardSection.INDICATORS] ?: DashboardSection.INDICATORS.defaultExpanded,
                        onToggle = onSectionToggle,
                        onCollapsedImpression = onCollapsedImpression,
                    ) {
                        NutritionDistributionWidget(metrics)
                        Text("已涵蓋 ${(metrics.diversityScore.value / 25).coerceIn(0, 4)}/4 類庫存分類", style = MaterialTheme.typography.bodySmall)
                        if (metrics.hasNoInventoryItems()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onAddIngredient) { Text("新增第一項食材") }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    DashboardFoldSection(
                        section = DashboardSection.RECOMMENDATIONS,
                        title = stringResource(Res.string.dashboard_recommendations),
                        expanded = state.sectionStates[DashboardSection.RECOMMENDATIONS] ?: DashboardSection.RECOMMENDATIONS.defaultExpanded,
                        onToggle = onSectionToggle,
                        onCollapsedImpression = onCollapsedImpression,
                    ) {
                        if (metrics.recommendations.isEmpty() && metrics.totalTrackedItems > 0) {
                            Text("目前沒有需要處理的庫存提醒。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        metrics.recommendations.forEach { recommendation ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Text(recommendation.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(onClick = { onRecommendationAction(recommendation) }) {
                                    Text(if (recommendation.reason == fridger.shared.health.RecommendationReason.EXPIRY_RISK) "用這項找食譜" else "挑食材加入購物清單")
                                }
                            }
                        }
                    }
                    metrics.trendMetadata?.let { metadata ->
                        Spacer(modifier = Modifier.height(24.dp))
                        DashboardFoldSection(
                            section = DashboardSection.HISTORY,
                            title = "庫存趨勢（${metadata.rangeDays} 天）",
                            expanded = state.sectionStates[DashboardSection.HISTORY] ?: DashboardSection.HISTORY.defaultExpanded,
                            onToggle = onSectionToggle,
                            onCollapsedImpression = onCollapsedImpression,
                        ) {
                            if (metadata.partialRange) {
                                Text("歷史自 ${metrics.trendSnapshots.firstOrNull()?.date ?: "今日"} 起累積；缺少的日期不補造資料。", style = MaterialTheme.typography.bodySmall)
                            }
                            if (metrics.trendSnapshots.isEmpty()) {
                                Text("尚無每日快照，之後每天會累積一筆。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                metrics.trendSnapshots.forEach { snapshot ->
                                    Text(
                                        "${snapshot.date} · ${snapshot.totalTrackedItems} 項 · 未分類 ${snapshot.unclassifiedPercent.toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(vertical = 3.dp),
                                    )
                                }
                            }
                        }
                            if (metrics.diversityHistory.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text("每週種類變化", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                metrics.diversityHistory.forEach { entry ->
                                    Text("${entry.weekStart} · 已涵蓋 ${(entry.score / 25).coerceIn(0, 4)}/4 類")
                                }
                            }
                            if (metrics.expiryHeatmap.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text("目前到期日期分布", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                metrics.expiryHeatmap.forEach { cell ->
                                    Text("${cell.date} · ${cell.category.displayName} · ${cell.count} 項")
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
private fun BentoGridContent(metrics: HealthDashboardMetrics, onViewDetails: () -> Unit, onAddIngredient: () -> Unit) {
    if (metrics.hasNoInventoryItems()) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text("冰箱目前是空的", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("新增食材後，這裡會顯示品項分類和到期日期。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onAddIngredient) { Text("新增食材") }
            }
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("冰箱庫存洞察 · 依品項筆數", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        // Row 1: Nutrition Distribution (Large) & Diversity (Medium)
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BentoCard(
                modifier = Modifier.weight(1.5f).fillMaxHeight(),
                title = "庫存品項比例",
                icon = Icons.Default.PieChart,
                color = MaterialTheme.colorScheme.primary
            ) {
                NutritionDistributionWidget(metrics)
            }
            
            BentoCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                title = "已涵蓋庫存分類",
                icon = Icons.Default.ColorLens,
                color = MaterialTheme.colorScheme.primary
            ) {
                DiversityScoreWidget(
                    score = metrics.diversityScore.value,
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

private fun HealthDashboardMetrics.hasNoInventoryItems(): Boolean =
    totalTrackedItems == 0 &&
        unclassifiedPercent == 0.0 &&
        nutritionDistribution.values.none { it > 0.0 } &&
        expiryAlerts.isEmpty()

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
private fun NutritionDistributionWidget(metrics: HealthDashboardMetrics) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val distribution = metrics.nutritionDistribution
        val sorted = NutritionCategory.entries.map { it to (distribution[it] ?: 0.0) }.sortedByDescending { it.second }
        sorted.forEachIndexed { index, (category, value) ->
            key(category) {
                NutritionItem(category.displayName, value, index)
            }
        }
        NutritionItem("未分類", metrics.unclassifiedPercent, sorted.size)
    }
}

@Composable
private fun NutritionItem(category: String, value: Double, index: Int) {
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
            text = category,
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
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Composable
private fun DiversityScoreWidget(
    score: Int,
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
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 8.dp
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(scoreAnim.value.roundToInt() / 25).coerceIn(0, 4)}/4 類",
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
private fun DashboardFoldSection(
    section: DashboardSection,
    title: String,
    expanded: Boolean,
    onToggle: (DashboardSection, Boolean) -> Unit,
    onCollapsedImpression: (DashboardSection, Boolean, Long) -> Unit,
    content: @Composable () -> Unit,
) {
    DisposableEffect(section, expanded) {
        val shownAt = Clock.System.now().toEpochMilliseconds()
        onDispose {
            val duration = Clock.System.now().toEpochMilliseconds() - shownAt
            if (!expanded && duration >= 1_000L) {
                onCollapsedImpression(section, expanded == section.defaultExpanded, duration)
            }
        }
    }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            TextButton(onClick = { onToggle(section, !expanded) }) { Text(if (expanded) "收合" else "展開") }
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
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
