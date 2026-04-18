package com.example.rooming.domain.usecase

import com.example.rooming.domain.repository.RoomsRepository
import javax.inject.Inject

class GetRoomsUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository,
) {
    operator fun invoke() = roomsRepository.getRooms()
}
