package com.example.rooming.domain.usecase

import com.example.rooming.domain.repository.BookingRepository
import javax.inject.Inject

class CancelBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(bookingId: String) = bookingRepository.cancelBooking(bookingId)
}
