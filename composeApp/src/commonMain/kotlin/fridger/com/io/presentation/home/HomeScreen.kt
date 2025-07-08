package fridger.com.io.presentation.home

import AddNewItemDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import fridger.com.io.ui.theme.spacing
import fridger.com.io.ui.theme.sizing
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = MaterialTheme.sizing.contentPaddingVertical),
    ) {
        // Header
        item {
            HomeHeader(onSettingsClick = onSettingsClick)
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))
        }

        // Expiry Section
        item {
            ExpirySection(
                todayItems = uiState.todayExpiringItems,
                weekItems = uiState.weekExpiringItems,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraHuge))
        }

        // Fridge Capacity
        item {
            FridgeCapacitySection(
                capacityPercentage = uiState.fridgeCapacityPercentage,
                onAddClick = viewModel::onAddNewItemClick,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraHuge))
        }

        // Refrigerated Items
        item {
            if (uiState.refrigeratedItems.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                        .padding(bottom = MaterialTheme.spacing.small),
                ) {
                    SectionTitle(title = stringResource(Res.string.home_refrigerated))
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                }
            }
        }

        items(
            items = uiState.refrigeratedItems,
            key = { it.id }
        ) { item ->
            RefrigeratedItemCard(
                item = item,
                onClick = { viewModel.onItemClick(item.id) },
                modifier = Modifier.padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        }

        // Frozen Items
        item {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
            if (uiState.refrigeratedItems.isNotEmpty()) { // TODO: Replace with frozen items
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                        .padding(bottom = MaterialTheme.spacing.small),
                ) {
                    SectionTitle(title = stringResource(Res.string.home_frozen))
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                }
            }
        }

        items(
            items = uiState.refrigeratedItems, // TODO: Replace with frozen items
            key = { "${it.id}_frozen" } // Temporary key to avoid conflicts
        ) { item ->
            RefrigeratedItemCard(
                item = item,
                onClick = { viewModel.onItemClick(item.id) },
                modifier = Modifier.padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        }
    }
}

@Composable
private fun HomeHeader(onSettingsClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = MaterialTheme.sizing.contentPaddingTop,
                    bottom = MaterialTheme.spacing.extraSmall,
                    start = MaterialTheme.sizing.contentPaddingHorizontal,
                    end = MaterialTheme.sizing.contentPaddingHorizontal
                ),
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
                .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal),
    ) {
        SectionTitle(title = stringResource(Res.string.home_expiring_soon))

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

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

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.large))

            ExpiryCard(
                icon = "",
                count = null,
                label = stringResource(Res.string.home_today),
                modifier = Modifier.weight(1f),
                isEmpty = true,
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))

        SectionTitle(title = stringResource(Res.string.home_expiring_this_week))

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

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

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.large))

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
        modifier = modifier.height(MaterialTheme.sizing.cardHeightMedium),
        shape = RoundedCornerShape(MaterialTheme.sizing.cornerRadiusExtraLarge),
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
                        .padding(MaterialTheme.spacing.large),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            ) {
                Text(
                    text = icon,
                    fontSize = MaterialTheme.sizing.iconExtraLarge.value.sp,
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
                        .padding(MaterialTheme.spacing.large),
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
    val isDarkTheme = MaterialTheme.colorScheme.background==AppColors.DarkBackground
    val progressTrackColor =
        if (isDarkTheme) AppColors.DarkProgressTrack else AppColors.ProgressTrack

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal),
    ) {
        SectionTitle(title = stringResource(Res.string.home_fridge_capacity))

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(MaterialTheme.sizing.cornerRadiusExtraLarge),
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
                        .padding(MaterialTheme.spacing.extraLarge),
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
                                .height(MaterialTheme.sizing.progressBarHeight)
                                .clip(RoundedCornerShape(MaterialTheme.sizing.cornerRadiusSmall))
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

                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.large))

                    Text(
                        text = "${(capacityPercentage * 100).toInt()}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

                OutlinedButton(
                    onClick = onAddClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MaterialTheme.sizing.cornerRadiusLarge),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(MaterialTheme.sizing.iconMedium),
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
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
private fun RefrigeratedItemCard(
    item: RefrigeratedItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MaterialTheme.sizing.cornerRadiusLarge),
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
                    .padding(MaterialTheme.spacing.large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon
            Box(
                modifier =
                    Modifier
                        .size(MaterialTheme.sizing.iconHuge)
                        .clip(CircleShape)
                        .background(AppColors.IconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.icon,
                    fontSize = MaterialTheme.sizing.iconLarge.value.sp,
                )
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.large))

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
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            ) {
                if (item.hasWarning) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(MaterialTheme.sizing.iconSmall),
                        tint = AppColors.Warning,
                    )
                }
                Text(
                    text = stringResourceFormat(
                        Res.string.home_days_until_expiry,
                        item.daysUntilExpiry
                    ),
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
