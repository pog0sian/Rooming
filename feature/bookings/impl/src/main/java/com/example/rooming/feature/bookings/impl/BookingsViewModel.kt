package com.example.rooming.feature.bookings.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rooming.core.common.CrashReporter
import com.example.rooming.domain.usecase.CancelBookingUseCase
import com.example.rooming.domain.usecase.GetMyBookingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class BookingsViewModel @Inject constructor(
    getMyBookingsUseCase: GetMyBookingsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    val uiState = getMyBookingsUseCase()
        .map { bookings ->
            BookingsUiState(
                bookings = bookings.sortedByDescending { booking -> booking.bookedAt },
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BookingsUiState(),
        )

    fun onCancelBooking(bookingId: String) {
        viewModelScope.launch {
            val result = runCatching {
                cancelBookingUseCase(bookingId)
            }.getOrElse { error ->
                crashReporter.log("BookingsViewModel.cancelBooking crashed")
                crashReporter.setKey("screen", "bookings")
                crashReporter.setKey("booking_id", bookingId)
                crashReporter.recordNonFatal(error)
                Result.failure(error)
            }

            result.onFailure { error ->
                crashReporter.log("BookingsViewModel.cancelBooking returned failure")
                crashReporter.setKey("screen", "bookings")
                crashReporter.setKey("booking_id", bookingId)
                crashReporter.recordNonFatal(error)
            }
        }
    }
}
