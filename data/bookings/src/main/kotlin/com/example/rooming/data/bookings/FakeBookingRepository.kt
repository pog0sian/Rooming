package com.example.rooming.data.bookings

import com.example.rooming.core.common.CrashReporter
import com.example.rooming.data.rooms.FakeRoomingStore
import com.example.rooming.domain.model.TimeSlot
import com.example.rooming.domain.repository.BookingRepository
import javax.inject.Inject

class FakeBookingRepository @Inject constructor(
    private val store: FakeRoomingStore,
    private val crashReporter: CrashReporter,
) : BookingRepository {
    override suspend fun bookRoom(roomId: String, timeSlot: TimeSlot) = runCatching {
        store.createBooking(roomId, timeSlot)
    }.onFailure { error ->
        crashReporter.log("FakeBookingRepository.bookRoom crashed")
        crashReporter.setKey("repository", "bookings")
        crashReporter.setKey("room_id", roomId)
        crashReporter.recordNonFatal(error)
    }.getOrElse { error ->
        Result.failure(error)
    }.onFailure { error ->
        crashReporter.log("FakeBookingRepository.bookRoom returned failure")
        crashReporter.setKey("repository", "bookings")
        crashReporter.setKey("room_id", roomId)
        crashReporter.recordNonFatal(error)
    }

    override fun getMyBookings() = store.observeBookings()

    override suspend fun cancelBooking(bookingId: String) = runCatching {
        store.cancelBooking(bookingId)
    }.onFailure { error ->
        crashReporter.log("FakeBookingRepository.cancelBooking crashed")
        crashReporter.setKey("repository", "bookings")
        crashReporter.setKey("booking_id", bookingId)
        crashReporter.recordNonFatal(error)
    }.getOrElse { error ->
        Result.failure(error)
    }.onFailure { error ->
        crashReporter.log("FakeBookingRepository.cancelBooking returned failure")
        crashReporter.setKey("repository", "bookings")
        crashReporter.setKey("booking_id", bookingId)
        crashReporter.recordNonFatal(error)
    }
}
