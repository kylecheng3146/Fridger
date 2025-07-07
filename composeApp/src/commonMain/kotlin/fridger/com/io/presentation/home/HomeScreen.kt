package fridger.com.io.presentation.home
import AddNewItemDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import fridger.com.io.presentation.ViewModelFactoryProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fridger.com.io.ui.theme.AppColors
import fridger.com.io.utils.stringResourceFormat
import fridger.composeapp.generated.resources.Res
import fridger.composeapp.generated.resources.home_add_ingredient
import fridger.composeapp.generated.resources.home_days_until_expiry
import fridger.composeapp.generated.resources.home_expiring_soon
import fridger.composeapp.generated.resources.home_expiring_this_week
import fridger.composeapp.generated.resources.home_fridge_capacity
import fridger.composeapp.generated.resources.home_frozen
import fridger.composeapp.generated.resources.home_refrigerated
import fridger.composeapp.generated.resources.home_this_week
import fridger.composeapp.generated.resources.home_title
import fridger.composeapp.generated.resources.home_today
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel(factory = ViewModelFactoryProvider.factory)
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showAddNewItemDialog) {
        AddNewItemDialog(
            onDismiss = viewModel::onDismissDialog,
            onConfirm = viewModel::onAddNewItemConfirm
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
    ) {
        // Header
        HomeHeader(onSettingsClick = onSettingsClick)

        Spacer(modifier = Modifier.height(24.dp))

        // Expiry Section
        ExpirySection(
            todayItems = uiState.todayExpiringItems,
            weekItems = uiState.weekExpiringItems,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Fridge Capacity
        FridgeCapacitySection(
            capacityPercentage = uiState.fridgeCapacityPercentage,
            onAddClick = viewModel::onAddNewItemClick,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Refrigerated Items
        RefrigeratedItemsSection(
            items = uiState.refrigeratedItems,
            onItemClick = viewModel::onItemClick,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Frozen Items
        FrozenItemsSection(
            items = uiState.refrigeratedItems, // TODO: Replace with frozen items
            onItemClick = viewModel::onItemClick,
        )
    }
}

@Composable
private fun HomeHeader(onSettingsClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 60.dp, bottom = 5.dp, start = 20.dp, end = 20.dp),
    ) {
        Text(
            text = stringResource(Res.string.home_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // Settings button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun ExpirySection(
    todayItems: List<ExpiringItem>,
    weekItems: List<ExpiringItem>,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
    ) {
        SectionTitle(title = stringResource(Res.string.home_expiring_soon))

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (todayItems.isNotEmpty()) {
                val item = todayItems.first()
                ExpiryCard(
                    icon = item.icon,
                    count = item.count,
                    label = stringResource(Res.string.home_today),
                    modifier = Modifier.weight(1f),
                )
            } else {
                ExpiryCard(
                    icon = "",
                    count = null,
                    label = stringResource(Res.string.home_today),
                    modifier = Modifier.weight(1f),
                    isEmpty = true,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            ExpiryCard(
                icon = "",
                count = null,
                label = stringResource(Res.string.home_today),
                modifier = Modifier.weight(1f),
                isEmpty = true,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle(title = stringResource(Res.string.home_expiring_this_week))

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (weekItems.isNotEmpty()) {
                val item = weekItems.first()
                ExpiryCard(
                    icon = item.icon,
                    count = item.count,
                    label = stringResource(Res.string.home_this_week),
                    modifier = Modifier.weight(1f),
                )
            } else {
                ExpiryCard(
                    icon = "",
                    count = null,
                    label = stringResource(Res.string.home_this_week),
                    modifier = Modifier.weight(1f),
                    isEmpty = true,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ExpiryCard(
    icon: String,
    count: Int?,
    label: String,
    modifier: Modifier = Modifier,
    isEmpty: Boolean = false,
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
            ),
    ) {
        if (!isEmpty) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = icon,
                    fontSize = 32.sp,
                )

                Column {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun FridgeCapacitySection(
    capacityPercentage: Float,
    onAddClick: () -> Unit,
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == AppColors.DarkBackground
    val progressTrackColor = if (isDarkTheme) AppColors.DarkProgressTrack else AppColors.ProgressTrack

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
    ) {
        SectionTitle(title = stringResource(Res.string.home_fridge_capacity))

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 2.dp,
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Custom progress bar
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(progressTrackColor)
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth(capacityPercentage)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.primary)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "${(capacityPercentage * 100).toInt()}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = onAddClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.home_add_ingredient),
                        fontSize = 16.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun RefrigeratedItemsSection(
    items: List<RefrigeratedItem>,
    onItemClick: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 10.dp),
    ) {
        SectionTitle(title = stringResource(Res.string.home_refrigerated))

        Spacer(modifier = Modifier.height(16.dp))

        items.forEach { item ->
            RefrigeratedItemCard(
                item = item,
                onClick = { onItemClick(item.id) },
            )

            if (item != items.last()) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun FrozenItemsSection(
    items: List<RefrigeratedItem>,
    onItemClick: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
    ) {
        SectionTitle(title = stringResource(Res.string.home_frozen))

        Spacer(modifier = Modifier.height(16.dp))

        items.forEach { item ->
            RefrigeratedItemCard(
                item = item,
                onClick = { onItemClick(item.id) },
            )

            if (item != items.last()) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun RefrigeratedItemCard(
    item: RefrigeratedItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp,
            ),
        onClick = onClick,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AppColors.IconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.icon,
                    fontSize = 24.sp,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name and Quantity
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = item.quantity,
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary,
                )
            }

            // Expiry info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (item.hasWarning) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppColors.Warning,
                    )
                }
                Text(
                    text = stringResourceFormat(Res.string.home_days_until_expiry, item.daysUntilExpiry),
                    fontSize = 14.sp,
                    color = if (item.hasWarning) AppColors.Warning else AppColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}
