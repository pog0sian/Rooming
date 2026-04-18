package com.example.rooming.domain.model

data class Booking(
    val id: String,
    val roomId: String,
    val roomName: String,
    val timeSlot: TimeSlot,
    val status: BookingStatus,
    val bookedAt: String,
)
