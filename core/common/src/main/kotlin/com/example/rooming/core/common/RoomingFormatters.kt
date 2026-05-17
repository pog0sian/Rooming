package com.example.rooming.core.common

import com.example.rooming.domain.model.BookingStatus
import com.example.rooming.domain.model.TimeSlot
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun TimeSlot.asDisplayLabel(): String = "$date • $startTime-$endTime"

fun BookingStatus.asDisplayLabel(): String = when (this) {
    BookingStatus.ACTIVE -> "Активно"
    BookingStatus.CANCELLED -> "Отменено"
}

fun String.asBookingCreatedAtLabel(): String = runCatching {
    LocalDateTime.parse(this).format(bookingCreatedAtFormatter)
}.getOrDefault(this)

private val bookingCreatedAtFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
