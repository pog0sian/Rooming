package com.example.rooming.domain.usecase

import com.example.rooming.domain.repository.FavoritesRepository
import javax.inject.Inject

class GetFavoriteRoomsUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    operator fun invoke() = favoritesRepository.getFavoriteRooms()
}
