@file:Suppress("ktlint:standard:no-wildcard-imports")

package fridger.com.io.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fridger.com.io.ui.theme.AppColors

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = remember { SettingsViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    // Update dark theme state from parent
    LaunchedEffect(isDarkTheme) {
        viewModel.onThemeChange(isDarkTheme)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        SettingsHeader(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(24.dp))

        // General Settings Section
        SettingsSection(title = "一般設定") {
            // Theme Setting
            SettingsItem(
                icon = Icons.Default.Brightness4,
                title = "主題",
                subtitle = if (uiState.isDarkTheme) "深色模式" else "淺色模式",
                onClick = {
                    val newTheme = !uiState.isDarkTheme
                    viewModel.onThemeChange(newTheme)
                    onThemeChange(newTheme)
                }
            ) {
                Switch(
                    checked = uiState.isDarkTheme,
                    onCheckedChange = { newTheme ->
                        viewModel.onThemeChange(newTheme)
                        onThemeChange(newTheme)
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // Notification Settings
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "過期提醒通知",
                subtitle = if (uiState.notificationEnabled) "已啟用" else "已停用",
                onClick = {
                    viewModel.onNotificationEnabledChange(!uiState.notificationEnabled)
                }
            ) {
                Switch(
                    checked = uiState.notificationEnabled,
                    onCheckedChange = viewModel::onNotificationEnabledChange
                )
            }

            if (uiState.notificationEnabled) {
                // Reminder Days
                var showReminderDialog by remember { mutableStateOf(false) }
                
                SettingsItem(
                    icon = Icons.Default.Schedule,
                    title = "提醒時間",
                    subtitle = "過期前 ${uiState.reminderDays} 天提醒",
                    onClick = { showReminderDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sound Setting
                SettingsItem(
                    icon = Icons.Default.VolumeUp,
                    title = "通知音效",
                    subtitle = if (uiState.soundEnabled) "已啟用" else "已停用",
                    onClick = {
                        viewModel.onSoundEnabledChange(!uiState.soundEnabled)
                    }
                ) {
                    Switch(
                        checked = uiState.soundEnabled,
                        onCheckedChange = viewModel::onSoundEnabledChange
                    )
                }

                // Vibration Setting
                SettingsItem(
                    icon = Icons.Default.PhoneAndroid,
                    title = "震動提醒",
                    subtitle = if (uiState.vibrationEnabled) "已啟用" else "已停用",
                    onClick = {
                        viewModel.onVibrationEnabledChange(!uiState.vibrationEnabled)
                    }
                ) {
                    Switch(
                        checked = uiState.vibrationEnabled,
                        onCheckedChange = viewModel::onVibrationEnabledChange
                    )
                }

                // Reminder Days Dialog
                if (showReminderDialog) {
                    ReminderDaysDialog(
                        currentDays = uiState.reminderDays,
                        onDismiss = { showReminderDialog = false },
                        onConfirm = { days ->
                            viewModel.onReminderDaysChange(days)
                            showReminderDialog = false
                        }
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // Language Setting
            var showLanguageDialog by remember { mutableStateOf(false) }
            
            SettingsItem(
                icon = Icons.Default.Language,
                title = "語言",
                subtitle = uiState.selectedLanguage.displayName,
                onClick = { showLanguageDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showLanguageDialog) {
                LanguageDialog(
                    currentLanguage = uiState.selectedLanguage,
                    onDismiss = { showLanguageDialog = false },
                    onLanguageSelect = { language ->
                        viewModel.onLanguageChange(language)
                        showLanguageDialog = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp, bottom = 5.dp, start = 20.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "設定",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    endContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        endContent?.invoke()
    }
}

@Composable
private fun ReminderDaysDialog(
    currentDays: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedDays by remember { mutableStateOf(currentDays) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "提醒時間設定")
        },
        text = {
            Column {
                Text(text = "選擇過期前幾天開始提醒：")
                Spacer(modifier = Modifier.height(16.dp))
                
                val dayOptions = listOf(1, 2, 3, 5, 7, 10, 14)
                dayOptions.forEach { days ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedDays = days }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDays == days,
                            onClick = { selectedDays = days }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "$days 天")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedDays) }
            ) {
                Text("確定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun LanguageDialog(
    currentLanguage: Language,
    onDismiss: () -> Unit,
    onLanguageSelect: (Language) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "選擇語言")
        },
        text = {
            Column {
                Language.values().forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onLanguageSelect(language) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLanguage == language,
                            onClick = { onLanguageSelect(language) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = language.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
