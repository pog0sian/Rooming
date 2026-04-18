package com.example.rooming.feature.favorites.impl

import com.example.rooming.domain.model.Room

data class FavoritesUiState(
    val rooms: List<Room> = emptyList(),
    val isLoading: Boolean = true,
)
