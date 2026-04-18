package com.example.rooming.feature.favorites.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rooming.core.ui.EmptyState
import com.example.rooming.core.ui.SectionCard
import com.example.rooming.domain.model.Room

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesRoute(
    onRoomClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesScreen(
        uiState = uiState,
        onRoomClick = onRoomClick,
        onRemoveFavorite = viewModel::onRemoveFavorite,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    onRoomClick: (String) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Favorites") }) },
    ) { innerPadding ->
        if (uiState.rooms.isEmpty() && !uiState.isLoading) {
            EmptyState(
                title = "No favorites yet",
                description = "Add rooms to favorites from the main list or details screen.",
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.rooms, key = Room::id) { room ->
                    SectionCard(
                        title = room.name,
                        subtitle = "${room.building} • Capacity ${room.capacity}",
                    ) {
                        Text(text = room.description, style = MaterialTheme.typography.bodyMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            OutlinedButton(onClick = { onRemoveFavorite(room.id) }) {
                                Text("Remove")
                            }
                            Button(onClick = { onRoomClick(room.id) }) {
                                Text("Open")
                            }
                        }
                    }
                }
            }
        }
    }
}
