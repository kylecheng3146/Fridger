package fridger.com.io.presentation.home.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fridger.com.io.presentation.home.HealthDashboardUiState
import fridger.shared.health.CalorieBucket
import fridger.shared.health.DiversityRating
import fridger.shared.health.ExpiryAlert
import fridger.shared.health.HealthDashboardMetrics
import fridger.shared.health.HealthRecommendation
import fridger.shared.health.NutritionCategory
import kotlin.math.roundToInt
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun HealthDashboardSummaryCard(
    state: HealthDashboardUiState,
    onRefresh: () -> Unit,
    onViewDetails: () -> Unit,
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
                    DashboardMetricsOverview(metrics = state.metrics)
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onViewDetails) {
                        Icon(Icons.Default.Visibility, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("查看詳情")
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
                colors = AssistChipDefaults.assistChipColors(
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
                state.metrics != null -> {
                    DashboardDetailContent(metrics = state.metrics)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DashboardDetailContent(metrics: HealthDashboardMetrics) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

        DetailSection(title = "系統建議") {
            if (metrics.recommendations.isEmpty()) {
                Text("目前沒有建議。")
            } else {
                RecommendationList(recommendations = metrics.recommendations)
            }
        }
    }
}

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

private fun formatTimestamp(epochMillis: Long): String {
    val dateTime = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "${dateTime.monthNumber}/${dateTime.dayOfMonth} $hour:$minute"
}
