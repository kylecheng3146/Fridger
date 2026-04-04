package fridger.com.io.presentation.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import fridger.com.io.presentation.home.RecipeSuggestion
import fridger.composeapp.generated.resources.Res
import fridger.composeapp.generated.resources.recipe_sheet_cooking_time
import fridger.composeapp.generated.resources.recipe_sheet_difficulty
import fridger.composeapp.generated.resources.recipe_sheet_generating
import fridger.composeapp.generated.resources.recipe_sheet_ingredients_title
import fridger.composeapp.generated.resources.recipe_sheet_instructions_title
import fridger.composeapp.generated.resources.recipe_sheet_servings_label
import fridger.composeapp.generated.resources.recipe_sheet_servings_unit
import fridger.composeapp.generated.resources.recipe_sheet_ai_generated_tag
import fridger.composeapp.generated.resources.recipe_sheet_try_again
import org.jetbrains.compose.resources.stringResource
import fridger.shared.recipe.RecipeFeedbackType

@Composable
fun RecipeResultSheet(
    isGenerating: Boolean,
    recipe: RecipeSuggestion?,
    onTryAgain: () -> Unit,
    onFeedbackClick: (RecipeFeedbackType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize()
    ) {
        AnimatedVisibility(
            visible = isGenerating,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Loading state
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.recipe_sheet_generating),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        AnimatedVisibility(
            visible = !isGenerating && recipe != null,
            enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it / 4 }) + fadeOut()
        ) {
            if (recipe != null) {
                // Recipe content
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Recipe title and description
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = recipe.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                AssistChip(
                                    onClick = {},
                                    label = { Text(stringResource(Res.string.recipe_sheet_ai_generated_tag)) },
                                    enabled = false,
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = recipe.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Recipe metadata
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MetadataPill(
                                label = stringResource(Res.string.recipe_sheet_cooking_time),
                                value = recipe.cookingTime,
                                modifier = Modifier.weight(1f),
                            )
                            MetadataPill(
                                label = stringResource(Res.string.recipe_sheet_difficulty),
                                value = recipe.difficulty,
                                modifier = Modifier.weight(1f),
                            )
                            MetadataPill(
                                label = stringResource(Res.string.recipe_sheet_servings_label),
                                value = "${recipe.servings} ${stringResource(Res.string.recipe_sheet_servings_unit)}",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    item {
                        val isLiked = recipe.userFeedback == RecipeFeedbackType.LIKE
                        val isDisliked = recipe.userFeedback == RecipeFeedbackType.DISLIKE

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            VoteActionButton(
                                selected = isLiked,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                icon = if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "讚",
                                onClick = { onFeedbackClick(RecipeFeedbackType.LIKE) },
                            )
                            VoteActionButton(
                                selected = isDisliked,
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedContentColor = MaterialTheme.colorScheme.onErrorContainer,
                                unselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                icon = if (isDisliked) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                                contentDescription = "倒讚",
                                onClick = { onFeedbackClick(RecipeFeedbackType.DISLIKE) },
                            )
                        }
                    }

                    // Ingredients section
                    item {
                        Text(
                            text = stringResource(Res.string.recipe_sheet_ingredients_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(recipe.ingredients) { ingredient ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = ingredient,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Instructions section
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(Res.string.recipe_sheet_instructions_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    itemsIndexed(recipe.instructions) { index, instruction ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = instruction,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (index < recipe.instructions.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Try again button
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(
                            onClick = onTryAgain,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(Res.string.recipe_sheet_try_again))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
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

@Composable
private fun VoteActionButton(
    selected: Boolean,
    selectedContainerColor: androidx.compose.ui.graphics.Color,
    selectedContentColor: androidx.compose.ui.graphics.Color,
    unselectedContainerColor: androidx.compose.ui.graphics.Color,
    unselectedContentColor: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = "voteActionScale",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) selectedContainerColor else unselectedContainerColor,
        label = "voteActionContainerColor",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) selectedContentColor else unselectedContentColor,
        label = "voteActionContentColor",
    )

    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp).scale(scale),
        shape = CircleShape,
        interactionSource = interactionSource,
        colors =
            IconButtonDefaults.filledIconButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
        )
    }
}
