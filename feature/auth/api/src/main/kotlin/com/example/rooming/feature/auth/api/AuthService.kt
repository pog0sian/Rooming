package com.example.rooming.feature.auth.api

interface AuthService {
    fun hasValidSession(): Boolean

    fun getSavedSession(): AuthSession?

    suspend fun completeLogin(
        provider: AuthProvider,
        token: String,
        userName: String,
    ): AuthResult

    fun logout()
}
