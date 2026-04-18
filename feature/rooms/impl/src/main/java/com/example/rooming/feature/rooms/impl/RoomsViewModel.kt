package com.example.rooming.feature.rooms.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rooming.domain.model.Room
import com.example.rooming.domain.model.TimeSlot
import com.example.rooming.domain.usecase.BookRoomUseCase
import com.example.rooming.domain.usecase.GetFavoriteRoomsUseCase
import com.example.rooming.domain.usecase.GetRoomByIdUseCase
import com.example.rooming.domain.usecase.GetRoomsUseCase
import com.example.rooming.domain.usecase.ToggleFavoriteRoomUseCase
import com.example.rooming.feature.rooms.api.RoomsFeatureApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RoomsViewModel @Inject constructor(
    getRoomsUseCase: GetRoomsUseCase,
    getFavoriteRoomsUseCase: GetFavoriteRoomsUseCase,
    private val toggleFavoriteRoomUseCase: ToggleFavoriteRoomUseCase,
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
            toggleFavoriteRoomUseCase(roomId)
        }
    }
}

@HiltViewModel
class RoomDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getFavoriteRoomsUseCase: GetFavoriteRoomsUseCase,
    private val getRoomByIdUseCase: GetRoomByIdUseCase,
    private val toggleFavoriteRoomUseCase: ToggleFavoriteRoomUseCase,
    private val bookRoomUseCase: BookRoomUseCase,
) : ViewModel() {
    private val roomId = checkNotNull(savedStateHandle.get<String>(RoomsFeatureApi.roomIdArg))
    private val refreshSignal = MutableStateFlow(0)
    private val messageState = MutableStateFlow<String?>(null)

    val uiState = combine(
        refreshSignal,
        getFavoriteRoomsUseCase(),
        messageState,
    ) { _, favoriteRooms, message ->
        val room = getRoomByIdUseCase(roomId)
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
            toggleFavoriteRoomUseCase(roomId)
        }
    }

    fun onBookClick(timeSlot: TimeSlot) {
        viewModelScope.launch {
            val result = bookRoomUseCase(roomId, timeSlot)
            messageState.value = result.fold(
                onSuccess = { "Booking created for ${timeSlot.startTime}" },
                onFailure = { error -> error.message ?: "Unable to book the room" },
            )
            refreshSignal.update { current -> current + 1 }
        }
    }

    fun consumeMessage() {
        messageState.value = null
    }
}
