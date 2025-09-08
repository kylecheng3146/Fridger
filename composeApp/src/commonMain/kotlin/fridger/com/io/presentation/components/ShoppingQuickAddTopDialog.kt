package fridger.com.io.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fridger.com.io.presentation.settings.SettingsManager
import fridger.com.io.presentation.home.NewItem
import fridger.com.io.presentation.home.IngredientIconMapper
import org.jetbrains.compose.resources.stringResource
import fridger.composeapp.generated.resources.Res
import fridger.composeapp.generated.resources.quick_add_add_selected
import fridger.composeapp.generated.resources.quick_add_clear_selection
import fridger.composeapp.generated.resources.quick_add_close
import fridger.composeapp.generated.resources.quick_add_fav_add
import fridger.composeapp.generated.resources.quick_add_fav_remove
import fridger.composeapp.generated.resources.quick_add_favorites
import fridger.composeapp.generated.resources.quick_add_none_selected
import fridger.composeapp.generated.resources.quick_add_pick_query
import fridger.composeapp.generated.resources.quick_add_quick_pick
import fridger.composeapp.generated.resources.quick_add_search_hint
import fridger.composeapp.generated.resources.quick_add_selected_prefix
import fridger.composeapp.generated.resources.quick_add_title
import kotlinx.coroutines.launch
import fridger.com.io.data.QuickAddCatalog
import fridger.com.io.ui.theme.sizing

@Composable
fun ShoppingQuickAddTopDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, quantity: String?) -> Unit = { _, _ -> },
    onItemsAdded: ((List<NewItem>) -> Unit)? = null,
    searchText: String? = null,
    onSearchTextChange: ((String) -> Unit)? = null,
    suggestions: List<String>? = null
) {
    data class QuickItem(val name: String, val icon: String)

    var localQuery by rememberSaveable { mutableStateOf("") }
    val query = searchText ?: localQuery

    val baseSuggestions: List<QuickItem> = remember(suggestions) {
        when {
            suggestions != null && suggestions.isNotEmpty() -> suggestions.map { name -> QuickItem(name, IngredientIconMapper.getIcon(name)) }
            else -> QuickAddCatalog.allNames.map { name -> QuickItem(name, IngredientIconMapper.getIcon(name)) }
        }
    }

    val filtered = remember(query, baseSuggestions) {
        if (query.isBlank()) baseSuggestions
        else baseSuggestions.filter { it.name.contains(query, ignoreCase = true) }
    }

    val favorites by SettingsManager.quickFavorites.collectAsState(initial = emptySet())
    val selected = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()

    var visible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(300, easing = FastOutSlowInEasing)),
        exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(250, easing = FastOutLinearInEasing)),
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(modifier = Modifier.fillMaxSize().clickable { visible = false }, color = Color.Transparent) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).clickable { },
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(top = 50.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                stringResource(Res.string.quick_add_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { visible = false }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.quick_add_close))
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = query,
                                onValueChange = { text ->
                                    if (onSearchTextChange != null) onSearchTextChange(text) else localQuery = text
                                },
                                label = { Text(stringResource(Res.string.quick_add_search_hint)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        if (query.isNotBlank()) {
                            OutlinedButton(
                                onClick = { if (!selected.contains(query)) selected.add(query) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(Res.string.quick_add_pick_query, query))
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        val favoriteList = remember(filtered, favorites) { filtered.filter { favorites.contains(it.name) } }
                        val otherList = remember(filtered, favorites) { filtered.filterNot { favorites.contains(it.name) } }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            if (favoriteList.isNotEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Text(
                                        stringResource(Res.string.quick_add_favorites),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                items(favoriteList.size) { index ->
                                    val item = favoriteList[index]
                                    QuickPickCell(
                                        name = item.name,
                                        icon = item.icon,
                                        isFavorite = true,
                                        isSelected = selected.contains(item.name),
                                        onToggleFavorite = { scope.launch { SettingsManager.toggleQuickFavorite(item.name) } }
                                    ) {
                                        if (selected.contains(item.name)) selected.remove(item.name) else selected.add(item.name)
                                    }
                                }
                                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(8.dp)) }
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Text(
                                        stringResource(Res.string.quick_add_quick_pick),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Text(
                                        stringResource(Res.string.quick_add_quick_pick),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            items(otherList.size) { index ->
                                val item = otherList[index]
                                QuickPickCell(
                                    name = item.name,
                                    icon = item.icon,
                                    isFavorite = favorites.contains(item.name),
                                    isSelected = selected.contains(item.name),
                                    onToggleFavorite = { scope.launch { SettingsManager.toggleQuickFavorite(item.name) } }
                                ) {
                                    if (selected.contains(item.name)) selected.remove(item.name) else selected.add(item.name)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            val statusText = if (selected.isEmpty()) {
                                stringResource(Res.string.quick_add_none_selected)
                            } else {
                                stringResource(Res.string.quick_add_selected_prefix) + selected.joinToString("、")
                            }
                            Text(text = statusText, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (selected.isNotEmpty()) {
                                TextButton(onClick = { selected.clear() }) { Text(stringResource(Res.string.quick_add_clear_selection)) }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val q: String? = null
                                if (onItemsAdded != null) {
                                    val items = selected.map { name -> NewItem(name = name, quantity = q, expiryDateDisplay = null) }
                                    onItemsAdded(items)
                                } else {
                                    selected.forEach { name -> onAdd(name, q) }
                                }
                                selected.clear()
                                visible = false
                            },
                            enabled = selected.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(Res.string.quick_add_add_selected))
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(visible) {
        if (!visible) {
            kotlinx.coroutines.delay(250)
            onDismiss()
        }
    }
}

@Composable
private fun QuickPickCell(
    name: String,
    icon: String,
    isFavorite: Boolean,
    isSelected: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp).padding(6.dp)) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(3.dp).size(28.dp).clickable(onClick = onToggleFavorite)) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(if (isFavorite) Res.string.quick_add_fav_remove else Res.string.quick_add_fav_add),
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center).size(18.dp)
                )
            }
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = icon, fontSize = MaterialTheme.sizing.iconHuge.value.sp, modifier = Modifier.padding(top = 6.dp))
                Spacer(Modifier.height(12.dp))
                Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
