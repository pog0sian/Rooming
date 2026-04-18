package com.example.rooming.domain.repository

import com.example.rooming.domain.model.Room
import kotlinx.coroutines.flow.Flow

interface RoomsRepository {
    fun getRooms(): Flow<List<Room>>

    suspend fun getRoomById(roomId: String): Room?
}
