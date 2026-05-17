package com.example.rooming.feature.rooms.impl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rooming.core.common.asDisplayLabel
import com.example.rooming.core.ui.EmptyState
import com.example.rooming.core.ui.InfoChipRow
import com.example.rooming.core.ui.SectionCard
import com.example.rooming.core.ui.R as UiR
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
    LaunchedEffect(Unit) {
        viewModel.onScreenViewed()
    }

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
    val roomsTitle = stringResource(UiR.string.rooms_title)
    val summaryTitle = stringResource(UiR.string.rooms_overview_title)
    val summarySubtitle = stringResource(UiR.string.rooms_overview_subtitle)
    val favoriteCountLabel = stringResource(UiR.string.favorites_count_label, uiState.favoriteIds.size)
    val roomCountLabel = stringResource(UiR.string.rooms_count_label, uiState.rooms.size)
    val freeSlotsLabel = stringResource(
        UiR.string.free_slots_count_label,
        uiState.rooms.sumOf { room -> room.availableTimeSlots.size },
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(roomsTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        if (uiState.rooms.isEmpty() && !uiState.isLoading) {
            EmptyState(
                title = stringResource(UiR.string.rooms_empty_title),
                description = stringResource(UiR.string.rooms_empty_description),
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
                item {
                    SectionCard(
                        title = summaryTitle,
                        subtitle = summarySubtitle,
                        modifier = Modifier.animateContentSize(),
                    ) {
                        InfoChipRow(
                            labels = listOf(roomCountLabel, favoriteCountLabel, freeSlotsLabel),
                        )
                    }
                }
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
    val backContentDescription = stringResource(UiR.string.back_content_description)
    val toggleFavoriteDescription = stringResource(UiR.string.toggle_favorite_content_description)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(room?.name ?: stringResource(UiR.string.room_details_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = backContentDescription,
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
                            contentDescription = toggleFavoriteDescription,
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        if (room == null && !uiState.isLoading) {
            EmptyState(
                title = stringResource(UiR.string.room_not_found_title),
                description = stringResource(UiR.string.room_not_found_description),
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
                        subtitle = stringResource(UiR.string.room_meta, it.building, it.capacity),
                    ) {
                        InfoChipRow(
                            labels = listOf(
                                stringResource(UiR.string.room_building_chip, it.building),
                                stringResource(UiR.string.room_capacity_chip, it.capacity),
                                stringResource(UiR.string.room_free_slots_chip, it.availableTimeSlots.size),
                            ),
                        )
                        Text(text = it.description, style = MaterialTheme.typography.bodyMedium)
                        InfoChipRow(
                            labels = it.equipment,
                        )
                    }
                    SectionCard(
                        title = stringResource(UiR.string.available_slots_title),
                        subtitle = stringResource(UiR.string.available_slots_subtitle),
                        modifier = Modifier.animateContentSize(),
                    ) {
                        val visibleTimeSlots = (it.availableTimeSlots + it.bookedTimeSlots)
                            .distinct()
                            .sortedWith(compareBy<TimeSlot> { timeSlot -> timeSlot.date }
                                .thenBy { timeSlot -> timeSlot.startTime })

                        if (visibleTimeSlots.isEmpty()) {
                            Text(
                                text = stringResource(UiR.string.no_slots_left),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        } else {
                            AnimatedVisibility(
                                visible = visibleTimeSlots.isNotEmpty(),
                                label = "available_slots_visibility",
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    visibleTimeSlots.forEach { timeSlot ->
                                        val isBooked = timeSlot in it.bookedTimeSlots
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text = timeSlot.asDisplayLabel(),
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                            TextButton(
                                                enabled = !isBooked,
                                                onClick = { onBookClick(timeSlot) },
                                            ) {
                                                Crossfade(
                                                    targetState = isBooked,
                                                    label = "booking_button_text",
                                                ) { booked ->
                                                    Text(
                                                        stringResource(
                                                            if (booked) {
                                                                UiR.string.booked_action
                                                            } else {
                                                                UiR.string.book_action
                                                            },
                                                        ),
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
    val toggleFavoriteDescription = stringResource(UiR.string.toggle_favorite_content_description)
    SectionCard(
        title = room.name,
        subtitle = stringResource(UiR.string.room_meta, room.building, room.capacity),
    ) {
        InfoChipRow(
            labels = listOf(
                stringResource(UiR.string.room_building_chip, room.building),
                stringResource(UiR.string.room_capacity_chip, room.capacity),
                stringResource(UiR.string.room_free_slots_chip, room.availableTimeSlots.size),
            ) + room.equipment.take(2),
        )
        Text(text = room.description, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = stringResource(UiR.string.free_today_label, room.availableTimeSlots.size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
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
                    contentDescription = toggleFavoriteDescription,
                )
            }
            OutlinedButton(onClick = onOpenDetails) {
                Text(stringResource(UiR.string.details_action))
            }
        }
    }
}
