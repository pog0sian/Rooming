package com.example.rooming.domain.repository

import com.example.rooming.domain.model.Booking
import com.example.rooming.domain.model.TimeSlot
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    suspend fun bookRoom(roomId: String, timeSlot: TimeSlot): Result<Booking>

    fun getMyBookings(): Flow<List<Booking>>

    suspend fun cancelBooking(bookingId: String): Result<Unit>
}
