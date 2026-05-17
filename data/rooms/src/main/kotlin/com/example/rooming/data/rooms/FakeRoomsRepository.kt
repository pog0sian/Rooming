package com.example.rooming.data.rooms

import com.example.rooming.core.common.CrashReporter
import com.example.rooming.domain.repository.RoomsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.catch

class FakeRoomsRepository @Inject constructor(
    private val store: FakeRoomingStore,
    private val crashReporter: CrashReporter,
) : RoomsRepository {
    override fun getRooms() = store.observeRooms()
        .catch { error ->
            crashReporter.log("FakeRoomsRepository.getRooms failed")
            crashReporter.setKey("repository", "rooms")
            crashReporter.recordNonFatal(error)
            emit(emptyList())
        }

    override suspend fun getRoomById(roomId: String) = runCatching {
        store.findRoomById(roomId)
    }.onFailure { error ->
        crashReporter.log("FakeRoomsRepository.getRoomById failed")
        crashReporter.setKey("repository", "rooms")
        crashReporter.setKey("room_id", roomId)
        crashReporter.recordNonFatal(error)
    }.getOrNull()
}
