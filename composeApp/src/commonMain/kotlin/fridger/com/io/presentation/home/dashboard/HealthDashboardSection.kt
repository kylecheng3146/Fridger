package fridger.com.io.presentation.home.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fridger.com.io.presentation.home.HealthDashboardUiState
import fridger.shared.health.CalorieBucket
import fridger.shared.health.DiversityRating
import fridger.shared.health.ExpiryAlert
import fridger.shared.health.ExpirySeverity
import fridger.shared.health.HealthDashboardMetrics
import fridger.shared.health.HealthRecommendation
import fridger.shared.health.NutritionCategory
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

private const val COLLAPSED_IMPRESSION_DELAY_MS = 1_500L
private val StatusGood = Color(0xFF2D5A27)
private val StatusWarning = Color(0xFFFF9800)
private val StatusCritical = Color(0xFFE53935)

@Composable
fun HealthDashboardSummaryCard(
    state: HealthDashboardUiState,
    onRefresh: () -> Unit,
    onViewDetails: () -> Unit,
    onToggleSection: (DashboardSection, Boolean) -> Unit,
    onCollapsedImpression: (DashboardSection, Boolean, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "健康儀表板",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "重新整理")
                }
            }

            state.lastUpdatedEpochMillis?.let {
                Text(
                    text = "上次更新：${formatTimestamp(it)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            when {
                state.isLoading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Column {
                        Text(
                            text = "無法載入儀表板資料",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onRefresh) {
                            Text("重試")
                        }
                    }
                }
                state.metrics != null -> {
                    val sectionStates = state.sectionStates
                    val metrics = state.metrics
                    DashboardMetricsOverview(metrics = metrics)
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardFoldableSection(
                            section = DashboardSection.INDICATORS,
                            title = "指標總覽",
                            isExpanded = sectionStates[DashboardSection.INDICATORS] ?: DashboardSection.INDICATORS.defaultExpanded,
                            onToggle = onToggleSection,
                            onCollapsedImpression = onCollapsedImpression,
                            containerColor = MaterialTheme.colorScheme.surface,
                            collapsedContent = { IndicatorCollapsedContent(metrics) },
                            expandedContent = { IndicatorsExpandedContent(metrics) },
                        )
                        DashboardFoldableSection(
                            section = DashboardSection.RECOMMENDATIONS,
                            title = "建議與補貨",
                            isExpanded = sectionStates[DashboardSection.RECOMMENDATIONS] ?: DashboardSection.RECOMMENDATIONS.defaultExpanded,
                            onToggle = onToggleSection,
                            onCollapsedImpression = onCollapsedImpression,
                            containerColor = MaterialTheme.colorScheme.surface,
                            collapsedContent = { RecommendationsCollapsedContent(metrics) },
                            expandedContent = { RecommendationsExpandedContent(metrics) },
                        )
                        DashboardFoldableSection(
                            section = DashboardSection.HISTORY,
                            title = "歷史趨勢",
                            isExpanded = sectionStates[DashboardSection.HISTORY] ?: DashboardSection.HISTORY.defaultExpanded,
                            onToggle = onToggleSection,
                            onCollapsedImpression = onCollapsedImpression,
                            containerColor = MaterialTheme.colorScheme.surface,
                            collapsedContent = { HistoryCollapsedContent(metrics) },
                            expandedContent = { HistoryExpandedContent(metrics) },
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        TextButton(onClick = onViewDetails) {
                            Icon(Icons.Default.Visibility, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("查看詳情")
                        }
                    }
                }
                else -> {
                    Text("目前沒有可顯示的儀表板資料。")
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricsOverview(metrics: HealthDashboardMetrics) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "營養比例",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        metrics.nutritionDistribution.entries
            .sortedByDescending { it.value }
            .take(3)
            .forEach { (category, percent) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = category.displayName,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = percent.roundToInt().toString() + "%",
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "多樣性：${metrics.diversityScore.rating.label()} (${metrics.diversityScore.value} 分)",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "即將過期：${metrics.expiryAlerts.size} 項",
            style = MaterialTheme.typography.bodyMedium,
        )

        if (metrics.recommendations.isNotEmpty()) {
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(metrics.recommendations.first().message) },
                colors =
                    AssistChipDefaults.assistChipColors(
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDashboardDetailSheet(
    state: HealthDashboardUiState,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onToggleSection: (DashboardSection, Boolean) -> Unit,
    onCollapsedImpression: (DashboardSection, Boolean, Long) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = "健康儀表板詳情",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("重新整理資料")
            }
            Spacer(modifier = Modifier.height(16.dp))

            when {
                state.isLoading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                else -> {
                    state.metrics?.let { metrics ->
                        DashboardDetailContent(
                            metrics = metrics,
                            sectionStates = state.sectionStates,
                            onToggleSection = onToggleSection,
                            onCollapsedImpression = onCollapsedImpression,
                        )
                    } ?: Text("目前沒有可顯示的儀表板資料。")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DashboardDetailContent(
    metrics: HealthDashboardMetrics,
    sectionStates: Map<DashboardSection, Boolean>,
    onToggleSection: (DashboardSection, Boolean) -> Unit,
    onCollapsedImpression: (DashboardSection, Boolean, Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DashboardFoldableSection(
            section = DashboardSection.INDICATORS,
            title = "指標總覽",
            isExpanded = sectionStates[DashboardSection.INDICATORS] ?: DashboardSection.INDICATORS.defaultExpanded,
            onToggle = onToggleSection,
            onCollapsedImpression = onCollapsedImpression,
            collapsedContent = { IndicatorCollapsedContent(metrics) },
            expandedContent = { IndicatorsExpandedContent(metrics) },
        )

        DashboardFoldableSection(
            section = DashboardSection.RECOMMENDATIONS,
            title = "建議與補貨",
            isExpanded = sectionStates[DashboardSection.RECOMMENDATIONS] ?: DashboardSection.RECOMMENDATIONS.defaultExpanded,
            onToggle = onToggleSection,
            onCollapsedImpression = onCollapsedImpression,
            collapsedContent = { RecommendationsCollapsedContent(metrics) },
            expandedContent = { RecommendationsExpandedContent(metrics) },
        )

        DashboardFoldableSection(
            section = DashboardSection.HISTORY,
            title = "歷史趨勢",
            isExpanded = sectionStates[DashboardSection.HISTORY] ?: DashboardSection.HISTORY.defaultExpanded,
            onToggle = onToggleSection,
            onCollapsedImpression = onCollapsedImpression,
            collapsedContent = { HistoryCollapsedContent(metrics) },
            expandedContent = { HistoryExpandedContent(metrics) },
        )
    }
}

@Composable
private fun DashboardFoldableSection(
    section: DashboardSection,
    title: String,
    isExpanded: Boolean,
    onToggle: (DashboardSection, Boolean) -> Unit,
    onCollapsedImpression: (DashboardSection, Boolean, Long) -> Unit,
    collapsedContent: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(section, !isExpanded) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onToggle(section, !isExpanded) }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "收合" else "展開",
                    )
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    expandedContent()
                }
            }
            AnimatedVisibility(visible = !isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    collapsedContent()
                }
            }
        }
    }
    LaunchedEffect(section, isExpanded) {
        if (!isExpanded) {
            delay(COLLAPSED_IMPRESSION_DELAY_MS)
            val isDefault = DashboardSectionDefaults.isDefaultState(section, isExpanded)
            onCollapsedImpression(section, isDefault, COLLAPSED_IMPRESSION_DELAY_MS)
        }
    }
}

@Composable
private fun IndicatorCollapsedContent(metrics: HealthDashboardMetrics) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(NutritionCategory.PRODUCE, NutritionCategory.PROTEIN, NutritionCategory.REFINED_GRAIN).forEach { category ->
                val percent = metrics.nutritionDistribution[category] ?: 0.0
                IndicatorBadge(
                    icon = nutritionIcon(category),
                    label = category.displayName,
                    value = "${percent.roundToInt()}%",
                    color = nutritionStatusColor(percent),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IndicatorBadge(
                icon = Icons.Default.ColorLens,
                label = "多樣性",
                value = "${metrics.diversityScore.value} 分",
                color = diversityStatusColor(metrics.diversityScore.value),
                modifier = Modifier.weight(1f),
            )
            IndicatorBadge(
                icon = Icons.Default.HourglassBottom,
                label = "即將過期",
                value = "${metrics.expiryAlerts.size} 項",
                color = expiryStatusColor(metrics.expiryAlerts.size),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun IndicatorBadge(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.08f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun IndicatorsExpandedContent(metrics: HealthDashboardMetrics) {
    DetailSection(title = "營養比例") {
        metrics.nutritionDistribution.entries
            .sortedByDescending { it.value }
            .forEach { (category, percent) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(category.displayName, modifier = Modifier.weight(1f))
                    Text("${percent.roundToInt()}%")
                }
            }
    }

    DetailSection(title = "膳食多樣性") {
        Text("評等：${metrics.diversityScore.rating.label()}")
        Text("分數：${metrics.diversityScore.value}")
    }

    DetailSection(title = "即將過期") {
        if (metrics.expiryAlerts.isEmpty()) {
            Text("暫無即將過期的食材。")
        } else {
            ExpiryAlertList(alerts = metrics.expiryAlerts)
        }
    }
}

@Composable
private fun RecommendationsCollapsedContent(metrics: HealthDashboardMetrics) {
    val topRecommendation = metrics.recommendations.firstOrNull()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = topRecommendation?.message ?: "一切均衡，暫無建議。",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun RecommendationsExpandedContent(metrics: HealthDashboardMetrics) {
    DetailSection(title = "系統建議") {
        if (metrics.recommendations.isEmpty()) {
            Text("目前沒有建議。")
        } else {
            RecommendationList(recommendations = metrics.recommendations)
        }
    }
}

@Composable
private fun HistoryCollapsedContent(metrics: HealthDashboardMetrics) {
    val metadata = metrics.trendMetadata
    val deficitSummary =
        metrics.trendSnapshots
            .firstOrNull()
            ?.deficitCategories
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString("、") { it.displayName }
    val summaryText =
        when {
            metadata != null && deficitSummary != null -> "近 ${metadata.rangeDays} 天偏少：$deficitSummary"
            metadata != null -> "近 ${metadata.rangeDays} 天趨勢穩定"
            else -> "尚無足夠的歷史資料"
        }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Timeline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = summaryText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun HistoryExpandedContent(metrics: HealthDashboardMetrics) {
    metrics.trendMetadata?.let {
        Text(
            text = "資料範圍：最近 ${it.rangeDays} 天${if (it.partialRange) "（統計中）" else ""}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } ?: Text(
        text = "尚未累積足夠的趨勢資料。",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    DetailSection(title = "營養趨勢") {
        if (metrics.trendSnapshots.isEmpty()) {
            Text("暫無趨勢資料。")
        } else {
            metrics.trendSnapshots.take(3).forEach { snapshot ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = formatDate(snapshot.date),
                        fontWeight = FontWeight.Medium,
                    )
                    val deficit =
                        snapshot.deficitCategories
                            .takeIf { it.isNotEmpty() }
                            ?.joinToString("、") { it.displayName } ?: "無"
                    Text(
                        text = "追蹤 ${snapshot.totalTrackedItems} 項 · 缺少 $deficit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    DetailSection(title = "多樣性歷史") {
        if (metrics.diversityHistory.isEmpty()) {
            Text("尚無多樣性歷史。")
        } else {
            metrics.diversityHistory.take(3).forEach { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(formatDate(entry.weekStart))
                    Text("${entry.score} 分 · ${entry.rating.label()}")
                }
            }
        }
    }

    if (metrics.expiryHeatmap.isNotEmpty()) {
        DetailSection(title = "過期熱度") {
            val grouped = metrics.expiryHeatmap.groupBy { it.severity }
            grouped.entries.forEach { (severity, cells) ->
                Text(
                    text = "${severity.label()}：${cells.sumOf { it.count }} 項",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private fun nutritionStatusColor(percent: Double): Color =
    when {
        percent >= 40 -> StatusGood
        percent >= 25 -> StatusWarning
        else -> StatusCritical
    }

private fun diversityStatusColor(score: Int): Color =
    when {
        score >= 70 -> StatusGood
        score >= 50 -> StatusWarning
        else -> StatusCritical
    }

private fun expiryStatusColor(count: Int): Color =
    when {
        count == 0 -> StatusGood
        count <= 2 -> StatusWarning
        else -> StatusCritical
    }

private fun nutritionIcon(category: NutritionCategory) =
    when (category) {
        NutritionCategory.PRODUCE -> Icons.Default.Eco
        NutritionCategory.PROTEIN -> Icons.Default.FitnessCenter
        NutritionCategory.REFINED_GRAIN -> Icons.Default.RamenDining
        NutritionCategory.OTHER -> Icons.Default.Lightbulb
    }

private fun formatDate(date: LocalDate): String = "${date.monthNumber}/${date.dayOfMonth}"

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun ExpiryAlertList(alerts: List<ExpiryAlert>) {
    LazyColumn(
        modifier = Modifier.heightIn(max = 200.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(alerts) { alert ->
            Column {
                Text(alert.itemName, fontWeight = FontWeight.Medium)
                Text(
                    text = "類別：${alert.category.displayName} · ${alert.daysUntilExpiry} 天內 · ${alert.calorieBucket.label()} 熱量",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun RecommendationList(recommendations: List<HealthRecommendation>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        recommendations.forEach {
            Text(
                text = "・${it.message}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun DiversityRating.label(): String =
    when (this) {
        DiversityRating.HIGH -> "高"
        DiversityRating.BALANCED -> "均衡"
        DiversityRating.LOW -> "低"
    }

private fun CalorieBucket.label(): String =
    when (this) {
        CalorieBucket.HIGH -> "高"
        CalorieBucket.MODERATE -> "中"
        CalorieBucket.LOW -> "低"
    }

private fun ExpirySeverity.label(): String =
    when (this) {
        ExpirySeverity.HIGH -> "高風險"
        ExpirySeverity.MEDIUM -> "中等"
        ExpirySeverity.LOW -> "低"
    }

private fun formatTimestamp(epochMillis: Long): String {
    val dateTime = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "${dateTime.monthNumber}/${dateTime.dayOfMonth} $hour:$minute"
}
