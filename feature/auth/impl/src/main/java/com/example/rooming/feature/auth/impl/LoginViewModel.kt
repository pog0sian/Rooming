package com.example.rooming.feature.auth.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rooming.core.analytics.AnalyticsService
import com.example.rooming.core.common.CrashReporter
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
    private val yandexProfileClient: YandexProfileClient,
    private val analytics: AnalyticsService,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onScreenViewed() {
        analytics.trackEvent(
            name = "screen_viewed",
            params = mapOf("screen_name" to "login"),
        )
        crashReporter.log("Login screen viewed")
        crashReporter.setKey("screen", "login")
    }

    fun onExternalLoginSuccess(
        provider: AuthProvider,
        token: String,
        userName: String,
        onLoggedIn: () -> Unit,
    ) {
        _uiState.update { state -> state.copy(isLoading = true, message = null) }
        viewModelScope.launch {
            val externalProfile = loadExternalProfile(
                provider = provider,
                token = token,
                fallbackUserName = userName,
            )

            runCatching {
                authService.completeLogin(
                    provider = provider,
                    token = token,
                    userName = externalProfile.name,
                    email = externalProfile.email,
                )
            }.onFailure { error ->
                val message = "Ошибка завершения входа через ${provider.displayName}"
                analytics.trackError(message, error)
                crashReporter.log("LoginViewModel.completeLogin failed")
                crashReporter.setKey("screen", "login")
                crashReporter.setKey("provider", provider.analyticsName)
                crashReporter.recordNonFatal(error)
                _uiState.update { state -> state.copy(isLoading = false, message = message) }
            }.onSuccess { result ->
                when (result) {
                    is AuthResult.Success -> {
                        analytics.trackEvent(
                            name = "user_logged_in",
                            params = mapOf("provider" to provider.analyticsName),
                        )
                        crashReporter.setUserId(result.session.asCrashReporterUserId())
                        _uiState.update { state -> state.copy(isLoading = false) }
                        onLoggedIn()
                    }
                    is AuthResult.Error -> {
                        analytics.trackError(result.message)
                        crashReporter.log("LoginViewModel.completeLogin returned error")
                        crashReporter.setKey("screen", "login")
                        crashReporter.setKey("provider", provider.analyticsName)
                        crashReporter.recordNonFatal(IllegalStateException(result.message))
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
        error?.let { throwable ->
            crashReporter.log("External login failed")
            crashReporter.setKey("screen", "login")
            crashReporter.setKey("provider", provider.analyticsName)
            crashReporter.recordNonFatal(throwable)
        }
        _uiState.update { state -> state.copy(isLoading = false, message = message) }
    }

    fun onExternalLoginCancelled(provider: AuthProvider) {
        _uiState.update { state ->
            state.copy(isLoading = false, message = "Вход через ${provider.displayName} отменён")
        }
    }

    private suspend fun loadExternalProfile(
        provider: AuthProvider,
        token: String,
        fallbackUserName: String,
    ): ExternalProfile {
        if (provider != AuthProvider.YANDEX) {
            return ExternalProfile(name = fallbackUserName, email = "")
        }

        return yandexProfileClient.getProfile(token)
            .onSuccess {
                crashReporter.log("Yandex profile loaded")
                crashReporter.setKey("provider", provider.analyticsName)
            }
            .onFailure { error ->
                analytics.trackError("Не удалось получить профиль Яндекса", error)
                crashReporter.log("Yandex profile request failed")
                crashReporter.setKey("provider", provider.analyticsName)
                crashReporter.recordNonFatal(error)
            }
            .getOrNull()
            ?.let { profile ->
                ExternalProfile(
                    name = profile.name.ifBlank { fallbackUserName },
                    email = profile.email,
                )
            }
            ?: ExternalProfile(name = fallbackUserName, email = "")
    }

    fun consumeMessage() {
        _uiState.update { state -> state.copy(message = null) }
    }
}

private data class ExternalProfile(
    val name: String,
    val email: String,
)

private fun com.example.rooming.feature.auth.api.AuthSession.asCrashReporterUserId(): String =
    "${provider.analyticsName}:${Integer.toHexString(token.hashCode())}"
