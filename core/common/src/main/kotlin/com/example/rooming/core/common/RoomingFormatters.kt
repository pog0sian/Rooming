package com.example.rooming.core.common

import com.example.rooming.domain.model.BookingStatus
import com.example.rooming.domain.model.TimeSlot

fun TimeSlot.asDisplayLabel(): String = "$date • $startTime-$endTime"

fun BookingStatus.asDisplayLabel(): String = when (this) {
    BookingStatus.ACTIVE -> "Активно"
    BookingStatus.CANCELLED -> "Отменено"
}
