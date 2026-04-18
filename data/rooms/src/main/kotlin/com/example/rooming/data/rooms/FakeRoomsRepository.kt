package com.example.rooming.data.rooms

import com.example.rooming.domain.repository.RoomsRepository
import javax.inject.Inject

class FakeRoomsRepository @Inject constructor(
    private val store: FakeRoomingStore,
) : RoomsRepository {
    override fun getRooms() = store.observeRooms()

    override suspend fun getRoomById(roomId: String) = store.findRoomById(roomId)
}
