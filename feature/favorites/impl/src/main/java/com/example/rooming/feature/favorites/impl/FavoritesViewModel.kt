package com.example.rooming.feature.favorites.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rooming.domain.usecase.GetFavoriteRoomsUseCase
import com.example.rooming.domain.usecase.ToggleFavoriteRoomUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    getFavoriteRoomsUseCase: GetFavoriteRoomsUseCase,
    private val toggleFavoriteRoomUseCase: ToggleFavoriteRoomUseCase,
) : ViewModel() {
    val uiState = getFavoriteRoomsUseCase()
        .map { favoriteRooms ->
            FavoritesUiState(
                rooms = favoriteRooms,
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState(),
        )

    fun onRemoveFavorite(roomId: String) {
        viewModelScope.launch {
            toggleFavoriteRoomUseCase(roomId)
        }
    }
}
