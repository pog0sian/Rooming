package com.example.rooming.data.favorites

import com.example.rooming.core.common.CrashReporter
import com.example.rooming.data.rooms.FakeRoomingStore
import com.example.rooming.domain.repository.FavoritesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

class FakeFavoritesRepository @Inject constructor(
    private val store: FakeRoomingStore,
    private val crashReporter: CrashReporter,
) : FavoritesRepository {
    override fun getFavoriteRooms() = combine(
        store.observeRooms(),
        store.observeFavoriteIds(),
    ) { rooms, favoriteIds ->
        rooms.filter { room -> room.id in favoriteIds }
    }.catch { error ->
        crashReporter.log("FakeFavoritesRepository.getFavoriteRooms failed")
        crashReporter.setKey("repository", "favorites")
        crashReporter.recordNonFatal(error)
        emit(emptyList())
    }

    override suspend fun toggleFavorite(roomId: String) {
        runCatching {
            store.toggleFavorite(roomId)
        }.onFailure { error ->
            crashReporter.log("FakeFavoritesRepository.toggleFavorite failed")
            crashReporter.setKey("repository", "favorites")
            crashReporter.setKey("room_id", roomId)
            crashReporter.recordNonFatal(error)
            throw error
        }
    }
}
