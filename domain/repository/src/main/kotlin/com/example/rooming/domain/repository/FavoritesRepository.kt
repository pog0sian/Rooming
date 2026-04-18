package com.example.rooming.domain.repository

import com.example.rooming.domain.model.Room
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun getFavoriteRooms(): Flow<List<Room>>

    suspend fun toggleFavorite(roomId: String)
}
