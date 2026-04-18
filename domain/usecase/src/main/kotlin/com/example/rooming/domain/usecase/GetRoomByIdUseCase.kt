package com.example.rooming.domain.usecase

import com.example.rooming.domain.repository.RoomsRepository
import javax.inject.Inject

class GetRoomByIdUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository,
) {
    suspend operator fun invoke(roomId: String) = roomsRepository.getRoomById(roomId)
}
