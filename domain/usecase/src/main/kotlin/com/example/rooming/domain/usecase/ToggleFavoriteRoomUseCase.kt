package com.example.rooming.domain.usecase

import com.example.rooming.domain.repository.FavoritesRepository
import javax.inject.Inject

class ToggleFavoriteRoomUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(roomId: String) = favoritesRepository.toggleFavorite(roomId)
}
