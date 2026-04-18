package com.example.rooming.feature.bookings.impl

import com.example.rooming.domain.model.Booking

data class BookingsUiState(
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = true,
)
