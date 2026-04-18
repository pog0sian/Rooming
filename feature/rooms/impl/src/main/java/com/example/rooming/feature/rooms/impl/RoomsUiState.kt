package com.example.rooming.feature.rooms.impl

import com.example.rooming.domain.model.Room

data class RoomsUiState(
    val rooms: List<Room> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
)

data class RoomDetailsUiState(
    val room: Room? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val message: String? = null,
)
