package com.example.rooming.domain.usecase

import com.example.rooming.domain.repository.BookingRepository
import javax.inject.Inject

class GetMyBookingsUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    operator fun invoke() = bookingRepository.getMyBookings()
}
