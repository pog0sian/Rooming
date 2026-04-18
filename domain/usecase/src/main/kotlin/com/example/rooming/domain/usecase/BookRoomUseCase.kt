package com.example.rooming.domain.usecase

import com.example.rooming.domain.model.TimeSlot
import com.example.rooming.domain.repository.BookingRepository
import javax.inject.Inject

class BookRoomUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(roomId: String, timeSlot: TimeSlot) =
        bookingRepository.bookRoom(roomId, timeSlot)
}
