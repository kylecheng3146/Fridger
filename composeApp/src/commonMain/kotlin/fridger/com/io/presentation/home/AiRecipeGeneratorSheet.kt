package fridger.com.io.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fridger.com.io.presentation.ViewModelFactoryProvider
import fridger.com.io.ui.theme.spacing

internal const val COLLAPSED_AI_RECIPE_SHEET_HEIGHT_FRACTION = 0.4f
internal const val EXPANDED_AI_RECIPE_SHEET_HEIGHT_FRACTION = 0.92f

internal fun isRecipeSaveFilled(uiState: AiRecipeUiState): Boolean =
    uiState.isSaved || uiState.isSavingRecipe

internal fun isRecipeSaveEnabled(uiState: AiRecipeUiState): Boolean =
    uiState.generatedRecipe!=null && !uiState.isSaved && !uiState.isSavingRecipe

internal fun aiRecipeSheetHeightFraction(uiState: AiRecipeUiState): Float =
    if (uiState.isLoading || uiState.generatedRecipe!=null || uiState.error!=null || uiState.isSaved) {
        EXPANDED_AI_RECIPE_SHEET_HEIGHT_FRACTION
    } else {
        COLLAPSED_AI_RECIPE_SHEET_HEIGHT_FRACTION
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiRecipeGeneratorSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    val viewModel: AiRecipeViewModel = viewModel(factory = ViewModelFactoryProvider.factory)
    val uiState by viewModel.uiState.collectAsState()
    val styles = listOf("中式", "西式", "韓式", "日式", "泰式")
    val animatedSheetHeightFraction by animateFloatAsState(
        targetValue = aiRecipeSheetHeightFraction(uiState),
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.9f),
        label = "aiRecipeSheetHeightFraction",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(180)),
        ) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onDismiss,
                        ),
                color = Color.Black.copy(alpha = 0.5f),
            ) {}
        }

        AnimatedVisibility(
            visible = visible,
            enter =
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                ) + fadeIn(animationSpec = tween(220)),
            exit =
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(260, easing = FastOutLinearInEasing),
                ) + fadeOut(animationSpec = tween(180)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animatedSheetHeightFraction)
                            .clickable(onClick = {}),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 16.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Surface(
                                modifier = Modifier.width(44.dp).height(5.dp),
                                shape = RoundedCornerShape(999.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
                            ) {}
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "變出一道菜",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                        Text(
                            text = "✨ AI 魔法：已自動為您盤點冰箱食材，為您量身打造專屬美味！",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                        Text("選擇您想吃的風味：", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            styles.forEach { style ->
                                val isSelected = uiState.selectedStyles.contains(style)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.toggleStyle(style) },
                                    label = { Text(style) },
                                    shape = RoundedCornerShape(50),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.generateRecipe() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = uiState.selectedStyles.isNotEmpty() && !uiState.isLoading,
                        ) {
                            Text("👨‍🍳 變出一道菜")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                        ) {
                            when {
                                uiState.isLoading -> {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        CircularProgressIndicator()
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("正在為您構思美味食譜...")
                                    }
                                }

                                uiState.generatedRecipe!=null -> {
                                    val recipe = uiState.generatedRecipe ?: return@Box

                                    Column(
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.Top,
                                        ) {
                                            if (uiState.selectedStyles.isNotEmpty()) {
                                                FlowRow(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                                ) {
                                                    uiState.selectedStyles.forEach { style ->
                                                        AssistChip(
                                                            onClick = {},
                                                            label = { Text(style) },
                                                            enabled = false,
                                                        )
                                                    }
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }

                                            IconButton(
                                                onClick = { viewModel.saveRecipe() },
                                                enabled = isRecipeSaveEnabled(uiState),
                                            ) {
                                                if (uiState.isSavingRecipe) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(20.dp),
                                                        strokeWidth = 2.dp,
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = if (isRecipeSaveFilled(uiState)) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                        contentDescription = if (uiState.isSaved) "已收藏食譜" else "收藏食譜",
                                                        tint = if (isRecipeSaveFilled(uiState)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                            }
                                        }

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                            ) {
                                                Text(
                                                    text = recipe.title,
                                                    style = MaterialTheme.typography.headlineSmall,
                                                )
                                                if (recipe.description.isNotBlank()) {
                                                    Text(
                                                        text = recipe.description,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                ) {
                                                    RecipeMetadataPill(
                                                        label = "時間",
                                                        value = recipe.cookingTime,
                                                        modifier = Modifier.weight(1f),
                                                    )
                                                    RecipeMetadataPill(
                                                        label = "難度",
                                                        value = recipe.difficulty,
                                                        modifier = Modifier.weight(1f),
                                                    )
                                                    RecipeMetadataPill(
                                                        label = "份量",
                                                        value = "${recipe.servings} 人份",
                                                        modifier = Modifier.weight(1f),
                                                    )
                                                }

                                                RecipeSectionTitle(title = "食材")
                                                recipe.ingredients.forEach { ingredient ->
                                                    RecipeBulletItem(text = ingredient)
                                                }

                                                RecipeSectionTitle(title = "步驟")
                                                recipe.instructions.forEachIndexed { index, instruction ->
                                                    RecipeStepItem(
                                                        stepNumber = index + 1,
                                                        text = instruction
                                                    )
                                                }
                                            }
                                        }

                                        uiState.error?.let { err ->
                                            Text(err, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }

                                uiState.error!=null -> {
                                    val errorMessage = uiState.error ?: return@Box

                                    Text(
                                        text = errorMessage,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.align(Alignment.Center),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun RecipeBulletItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(text = "•", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RecipeStepItem(
    stepNumber: Int,
    text: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "$stepNumber.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RecipeMetadataPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
