package com.example.rooming.feature.auth.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rooming.core.analytics.AnalyticsService
import com.example.rooming.feature.auth.api.AuthProvider
import com.example.rooming.feature.auth.api.AuthResult
import com.example.rooming.feature.auth.api.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService,
    private val analytics: AnalyticsService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onScreenViewed() {
        analytics.trackEvent(
            name = "screen_viewed",
            params = mapOf("screen_name" to "login"),
        )
    }

    fun onExternalLoginSuccess(
        provider: AuthProvider,
        token: String,
        userName: String,
        onLoggedIn: () -> Unit,
    ) {
        _uiState.update { state -> state.copy(isLoading = true, message = null) }
        viewModelScope.launch {
            when (val result = authService.completeLogin(provider, token, userName)) {
                is AuthResult.Success -> {
                    analytics.trackEvent(
                        name = "user_logged_in",
                        params = mapOf("provider" to provider.analyticsName),
                    )
                    _uiState.update { state -> state.copy(isLoading = false) }
                    onLoggedIn()
                }
                is AuthResult.Error -> {
                    analytics.trackError(result.message)
                    _uiState.update { state ->
                        state.copy(isLoading = false, message = result.message)
                    }
                }
                AuthResult.Cancelled -> {
                    _uiState.update { state ->
                        state.copy(isLoading = false, message = "Вход отменён")
                    }
                }
            }
        }
    }

    fun onExternalLoginError(provider: AuthProvider, error: Throwable?) {
        val errorText = error?.message.orEmpty()
        val message = if (
            provider == AuthProvider.VK &&
            (errorText.contains("Security Error", ignoreCase = true) ||
                errorText.contains("invalid_request", ignoreCase = true))
        ) {
            "VK отклонил вход. Проверьте ID приложения, пакет, MainActivity и SHA-1 в кабинете VK."
        } else {
            "Не удалось войти через ${provider.displayName}"
        }
        analytics.trackError(message, error)
        _uiState.update { state -> state.copy(isLoading = false, message = message) }
    }

    fun onExternalLoginCancelled(provider: AuthProvider) {
        _uiState.update { state ->
            state.copy(isLoading = false, message = "Вход через ${provider.displayName} отменён")
        }
    }

    fun consumeMessage() {
        _uiState.update { state -> state.copy(message = null) }
    }
}
