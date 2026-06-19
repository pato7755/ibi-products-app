package com.task.ibiproductsapp.presentation.favorite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.task.ibiproductsapp.R
import com.task.ibiproductsapp.domain.model.Product
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onProductClick: (Int) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val state by viewModel.favoritesState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.favorites)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.favorites.isEmpty() -> {
                    EmptyFavoritesState(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    // Filter out the pending-removal item from the visible list
                    val visibleFavorites = state.favorites.filter {
                        !state.pendingRemovalIds.contains(it.id)
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = visibleFavorites,
                            key = { it.id }
                        ) { product ->
                            val removedMessage = stringResource(R.string.removed_from_favorites, product.title)
                            val undoLabel = stringResource(R.string.undo)

                            FavoriteItemCard(
                                product = product,
                                onClick = { onProductClick(product.id) },
                                onRemoveClick = {
                                    viewModel.removeFavorite(product.id)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = removedMessage,
                                            actionLabel = undoLabel,
                                            duration = SnackbarDuration.Long
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoRemove(product.id)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteItemCard(
    product: Product,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.thumbnail,
                contentDescription = product.title,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onRemoveClick) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = stringResource(R.string.remove_from_favorites),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyFavoritesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_favorites_yet),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.tap_the_heart_on_any_product_to_save_it_here),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
