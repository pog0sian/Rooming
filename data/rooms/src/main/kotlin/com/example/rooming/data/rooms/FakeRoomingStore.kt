package com.example.rooming.data.rooms

import com.example.rooming.domain.model.Booking
import com.example.rooming.domain.model.BookingStatus
import com.example.rooming.domain.model.Room
import com.example.rooming.domain.model.TimeSlot
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeRoomingStore @Inject constructor() {
    private val baseRooms = sampleRooms()
    private val nextBookingId = AtomicInteger(3)

    private val favoriteIdsState = MutableStateFlow(setOf("room-101", "room-303"))
    private val bookingsState = MutableStateFlow(
        listOf(
            Booking(
                id = "booking-1",
                roomId = "room-202",
                roomName = "Room 202",
                timeSlot = TimeSlot("2026-04-18", "10:00", "11:30"),
                status = BookingStatus.ACTIVE,
                bookedAt = "2026-04-17T18:20:00",
            ),
            Booking(
                id = "booking-2",
                roomId = "room-404",
                roomName = "Room 404",
                timeSlot = TimeSlot("2026-04-18", "17:00", "18:30"),
                status = BookingStatus.ACTIVE,
                bookedAt = "2026-04-17T19:10:00",
            ),
        ),
    )

    fun observeRooms(): Flow<List<Room>> = bookingsState.map(::applyBookingsToRooms)

    fun observeFavoriteIds(): StateFlow<Set<String>> = favoriteIdsState.asStateFlow()

    fun observeBookings(): StateFlow<List<Booking>> = bookingsState.asStateFlow()

    suspend fun findRoomById(roomId: String): Room? =
        observeRooms().first().firstOrNull { room -> room.id == roomId }

    suspend fun toggleFavorite(roomId: String) {
        favoriteIdsState.update { favoriteIds ->
            if (roomId in favoriteIds) favoriteIds - roomId else favoriteIds + roomId
        }
    }

    suspend fun createBooking(roomId: String, timeSlot: TimeSlot): Result<Booking> {
        val room = findRoomById(roomId)
            ?: return Result.failure(IllegalArgumentException("Room not found: $roomId"))

        if (timeSlot !in room.availableTimeSlots) {
            return Result.failure(IllegalStateException("Time slot is no longer available"))
        }

        val booking = Booking(
            id = "booking-${nextBookingId.incrementAndGet()}",
            roomId = room.id,
            roomName = room.name,
            timeSlot = timeSlot,
            status = BookingStatus.ACTIVE,
            bookedAt = LocalDateTime.now().toString(),
        )

        bookingsState.update { bookings -> bookings + booking }
        return Result.success(booking)
    }

    suspend fun cancelBooking(bookingId: String): Result<Unit> {
        val hasBooking = bookingsState.value.any { booking -> booking.id == bookingId }
        if (!hasBooking) {
            return Result.failure(IllegalArgumentException("Booking not found: $bookingId"))
        }

        bookingsState.update { bookings ->
            bookings.map { booking ->
                if (booking.id == bookingId) {
                    booking.copy(status = BookingStatus.CANCELLED)
                } else {
                    booking
                }
            }
        }
        return Result.success(Unit)
    }

    private fun applyBookingsToRooms(bookings: List<Booking>): List<Room> {
        val activeSlotsByRoomId = bookings
            .filter { booking -> booking.status == BookingStatus.ACTIVE }
            .groupBy(keySelector = Booking::roomId, valueTransform = Booking::timeSlot)

        return baseRooms.map { room ->
            room.copy(
                availableTimeSlots = room.availableTimeSlots.filterNot { timeSlot ->
                    timeSlot in activeSlotsByRoomId[room.id].orEmpty()
                },
            )
        }
    }
}
