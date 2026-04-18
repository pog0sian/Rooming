package com.example.rooming.data.favorites

import com.example.rooming.data.rooms.FakeRoomingStore
import com.example.rooming.domain.repository.FavoritesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.combine

class FakeFavoritesRepository @Inject constructor(
    private val store: FakeRoomingStore,
) : FavoritesRepository {
    override fun getFavoriteRooms() = combine(
        store.observeRooms(),
        store.observeFavoriteIds(),
    ) { rooms, favoriteIds ->
        rooms.filter { room -> room.id in favoriteIds }
    }

    override suspend fun toggleFavorite(roomId: String) {
        store.toggleFavorite(roomId)
    }
}
