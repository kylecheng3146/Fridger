package fridger.com.io.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fridger.com.io.presentation.home.RecipeSuggestion
import fridger.composeapp.generated.resources.Res
import fridger.composeapp.generated.resources.recipe_sheet_close
import fridger.composeapp.generated.resources.recipe_sheet_cooking_time
import fridger.composeapp.generated.resources.recipe_sheet_difficulty
import fridger.composeapp.generated.resources.recipe_sheet_generating
import fridger.composeapp.generated.resources.recipe_sheet_ingredients_title
import fridger.composeapp.generated.resources.recipe_sheet_instructions_title
import fridger.composeapp.generated.resources.recipe_sheet_servings
import fridger.composeapp.generated.resources.recipe_sheet_title
import fridger.composeapp.generated.resources.recipe_sheet_try_again
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecipeResultSheet(
    isGenerating: Boolean,
    recipe: RecipeSuggestion?,
    onTryAgain: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize()
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.recipe_sheet_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.recipe_sheet_close))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                            Text(
                                text = recipe.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
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
                            Text(
                                text =
                                    stringResource(
                                        Res.string.recipe_sheet_cooking_time,
                                        recipe.cookingTime
                                    ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text =
                                    stringResource(
                                        Res.string.recipe_sheet_difficulty,
                                        recipe.difficulty
                                    ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text =
                                    stringResource(
                                        Res.string.recipe_sheet_servings,
                                        recipe.servings
                                    ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
