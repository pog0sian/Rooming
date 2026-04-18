package com.example.rooming.feature.rooms.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rooming.core.common.asDisplayLabel
import com.example.rooming.core.ui.EmptyState
import com.example.rooming.core.ui.SectionCard
import com.example.rooming.domain.model.Room
import com.example.rooming.domain.model.TimeSlot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsRoute(
    onRoomClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoomsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RoomsScreen(
        uiState = uiState,
        onRoomClick = onRoomClick,
        onFavoriteClick = viewModel::onFavoriteClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailsRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoomDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    RoomDetailsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onFavoriteClick = viewModel::onFavoriteClick,
        onBookClick = viewModel::onBookClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(
    uiState: RoomsUiState,
    onRoomClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Rooms") })
        },
    ) { innerPadding ->
        if (uiState.rooms.isEmpty() && !uiState.isLoading) {
            EmptyState(
                title = "No rooms available",
                description = "Fake repository returned an empty schedule.",
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
                    RoomCard(
                        room = room,
                        isFavorite = room.id in uiState.favoriteIds,
                        onFavoriteClick = { onFavoriteClick(room.id) },
                        onOpenDetails = { onRoomClick(room.id) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailsScreen(
    uiState: RoomDetailsUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onBookClick: (TimeSlot) -> Unit,
    modifier: Modifier = Modifier,
) {
    val room = uiState.room

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(room?.name ?: "Room details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (uiState.isFavorite) {
                                Icons.Outlined.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = "Toggle favorite",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        if (room == null && !uiState.isLoading) {
            EmptyState(
                title = "Room not found",
                description = "The requested room is missing in fake data.",
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                room?.let {
                    SectionCard(
                        title = it.name,
                        subtitle = "${it.building} • Capacity ${it.capacity}",
                    ) {
                        Text(text = it.description, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Equipment: ${it.equipment.joinToString()}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    SectionCard(
                        title = "Available time slots",
                        subtitle = "Book a free slot directly from details",
                    ) {
                        if (it.availableTimeSlots.isEmpty()) {
                            Text(
                                text = "There are no free slots left for today.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                it.availableTimeSlots.forEach { timeSlot ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = timeSlot.asDisplayLabel(),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        TextButton(onClick = { onBookClick(timeSlot) }) {
                                            Text("Book")
                                        }
                                    }
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
private fun RoomCard(
    room: Room,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    SectionCard(
        title = room.name,
        subtitle = "${room.building} • Capacity ${room.capacity}",
    ) {
        Text(text = room.description, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "Free today: ${room.availableTimeSlots.size} slot(s)",
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) {
                        Icons.Outlined.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = "Toggle favorite",
                )
            }
            Button(onClick = onOpenDetails) {
                Text("Details")
            }
        }
    }
}
