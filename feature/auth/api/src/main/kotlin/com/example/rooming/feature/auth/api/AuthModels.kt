package com.example.rooming.feature.auth.api

enum class AuthProvider(val analyticsName: String, val displayName: String) {
    YANDEX(analyticsName = "yandex", displayName = "Яндекс ID"),
    VK(analyticsName = "vk", displayName = "VK"),
}

data class AuthSession(
    val token: String,
    val userName: String,
    val email: String = "",
    val provider: AuthProvider,
)

sealed interface AuthResult {
    data class Success(val session: AuthSession) : AuthResult
    data class Error(val message: String) : AuthResult
    data object Cancelled : AuthResult
}

data class AuthConfig(
    val yandexClientId: String,
    val vkClientId: String,
    val vkClientSecret: String,
)
