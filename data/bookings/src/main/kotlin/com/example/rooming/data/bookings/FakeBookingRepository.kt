package com.example.rooming.data.bookings

import com.example.rooming.data.rooms.FakeRoomingStore
import com.example.rooming.domain.model.TimeSlot
import com.example.rooming.domain.repository.BookingRepository
import javax.inject.Inject

class FakeBookingRepository @Inject constructor(
    private val store: FakeRoomingStore,
) : BookingRepository {
    override suspend fun bookRoom(roomId: String, timeSlot: TimeSlot) =
        store.createBooking(roomId, timeSlot)

    override fun getMyBookings() = store.observeBookings()

    override suspend fun cancelBooking(bookingId: String) =
        store.cancelBooking(bookingId)
}
