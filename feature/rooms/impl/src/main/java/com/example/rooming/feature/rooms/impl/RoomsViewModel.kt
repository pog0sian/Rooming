package com.example.rooming.feature.rooms.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rooming.core.analytics.AnalyticsService
import com.example.rooming.core.common.CrashReporter
import com.example.rooming.domain.model.Room
import com.example.rooming.domain.model.TimeSlot
import com.example.rooming.domain.usecase.BookRoomUseCase
import com.example.rooming.domain.usecase.GetFavoriteRoomsUseCase
import com.example.rooming.domain.usecase.GetRoomsUseCase
import com.example.rooming.domain.usecase.ToggleFavoriteRoomUseCase
import com.example.rooming.feature.rooms.api.RoomsFeatureApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RoomsViewModel @Inject constructor(
    getRoomsUseCase: GetRoomsUseCase,
    getFavoriteRoomsUseCase: GetFavoriteRoomsUseCase,
    private val toggleFavoriteRoomUseCase: ToggleFavoriteRoomUseCase,
    private val analytics: AnalyticsService,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    val uiState = combine(
        getRoomsUseCase(),
        getFavoriteRoomsUseCase(),
    ) { rooms, favoriteRooms ->
        RoomsUiState(
            rooms = rooms,
            favoriteIds = favoriteRooms.map(Room::id).toSet(),
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RoomsUiState(),
    )

    fun onFavoriteClick(roomId: String) {
        viewModelScope.launch {
            runCatching {
                toggleFavoriteRoomUseCase(roomId)
            }.onFailure { error ->
                analytics.trackError("Не удалось изменить избранное", error)
                crashReporter.log("RoomsViewModel.toggleFavorite failed")
                crashReporter.setKey("screen", "rooms")
                crashReporter.setKey("room_id", roomId)
                crashReporter.recordNonFatal(error)
            }
        }
    }

    fun onScreenViewed() {
        analytics.trackEvent(
            name = "screen_viewed",
            params = mapOf("screen_name" to "rooms"),
        )
        crashReporter.log("Rooms screen viewed")
        crashReporter.setKey("screen", "rooms")
    }
}

@HiltViewModel
class RoomDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getRoomsUseCase: GetRoomsUseCase,
    getFavoriteRoomsUseCase: GetFavoriteRoomsUseCase,
    private val toggleFavoriteRoomUseCase: ToggleFavoriteRoomUseCase,
    private val bookRoomUseCase: BookRoomUseCase,
    private val analytics: AnalyticsService,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    private val roomId = checkNotNull(savedStateHandle.get<String>(RoomsFeatureApi.roomIdArg))
    private val rooms = getRoomsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )
    private val messageState = MutableStateFlow<String?>(null)
    private val pendingBookedSlots = MutableStateFlow<Set<TimeSlot>>(emptySet())
    private val confirmedBookedSlots = MutableStateFlow<Set<TimeSlot>>(emptySet())

    init {
        viewModelScope.launch {
            rooms.collectLatest { rooms ->
                val sourceBookedSlots = rooms
                    .firstOrNull { room -> room.id == roomId }
                    ?.bookedTimeSlots
                    .orEmpty()
                    .toSet()
                confirmedBookedSlots.update { slots -> slots - sourceBookedSlots }
            }
        }
    }

    val uiState = combine(
        rooms,
        getFavoriteRoomsUseCase(),
        messageState,
        pendingBookedSlots,
        confirmedBookedSlots,
    ) { rooms, favoriteRooms, message, pendingSlots, confirmedSlots ->
        val localBookedSlots = pendingSlots + confirmedSlots
        val room = rooms.firstOrNull { room -> room.id == roomId }?.let { room ->
            room.copy(
                availableTimeSlots = room.availableTimeSlots.filterNot { timeSlot ->
                    timeSlot in localBookedSlots
                },
                bookedTimeSlots = (room.bookedTimeSlots + localBookedSlots).distinct(),
            )
        }
        RoomDetailsUiState(
            room = room,
            isFavorite = favoriteRooms.any { favoriteRoom -> favoriteRoom.id == roomId },
            isLoading = false,
            message = message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RoomDetailsUiState(),
    )

    fun onFavoriteClick() {
        viewModelScope.launch {
            runCatching {
                toggleFavoriteRoomUseCase(roomId)
            }.onFailure { error ->
                analytics.trackError("Не удалось изменить избранное", error)
                crashReporter.log("RoomDetailsViewModel.toggleFavorite failed")
                crashReporter.setKey("screen", "room_details")
                crashReporter.setKey("room_id", roomId)
                crashReporter.recordNonFatal(error)
            }
        }
    }

    fun onBookClick(timeSlot: TimeSlot) {
        if (timeSlot in pendingBookedSlots.value) return
        if (timeSlot in confirmedBookedSlots.value) return
        pendingBookedSlots.update { slots -> slots + timeSlot }

        viewModelScope.launch {
            val result = runCatching {
                bookRoomUseCase(roomId, timeSlot)
            }.getOrElse { error ->
                Result.failure(error)
            }
            result.onSuccess {
                analytics.trackEvent(
                    name = "room_booked",
                    params = mapOf("room_id" to roomId, "slot" to timeSlot.startTime),
                )
                confirmedBookedSlots.update { slots -> slots + timeSlot }
            }.onFailure { error ->
                if (error.isSlotUnavailable()) {
                    confirmedBookedSlots.update { slots -> slots + timeSlot }
                }
                analytics.trackError("Не удалось забронировать аудиторию", error)
                crashReporter.log("RoomDetailsViewModel.bookRoom failed")
                crashReporter.setKey("screen", "room_details")
                crashReporter.setKey("room_id", roomId)
                crashReporter.setKey("slot", timeSlot.startTime)
                crashReporter.recordNonFatal(error)
            }
            messageState.value = result.fold(
                onSuccess = { "Бронирование оформлено на ${timeSlot.startTime}" },
                onFailure = { error -> error.message ?: "Не удалось забронировать аудиторию" },
            )
            pendingBookedSlots.update { slots -> slots - timeSlot }
        }
    }

    fun consumeMessage() {
        messageState.value = null
    }
}

private fun Throwable.isSlotUnavailable(): Boolean {
    val message = message.orEmpty()
    return message.contains("no longer available", ignoreCase = true) ||
        message.contains("unavailable", ignoreCase = true) ||
        message.contains("недоступ", ignoreCase = true)
}
