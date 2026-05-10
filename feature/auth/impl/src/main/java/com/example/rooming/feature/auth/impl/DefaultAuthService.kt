package com.example.rooming.feature.auth.impl

import com.example.rooming.feature.auth.api.AuthProvider
import com.example.rooming.feature.auth.api.AuthResult
import com.example.rooming.feature.auth.api.AuthService
import com.example.rooming.feature.auth.api.AuthSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAuthService @Inject constructor(
    private val storage: SecureAuthStorage,
) : AuthService {
    override fun hasValidSession(): Boolean = storage.read() != null

    override fun getSavedSession(): AuthSession? = storage.read()

    override suspend fun completeLogin(
        provider: AuthProvider,
        token: String,
        userName: String,
    ): AuthResult {
        if (token.isBlank()) {
            return AuthResult.Error("SDK авторизации вернул пустой токен")
        }

        val session = AuthSession(
            token = token,
            userName = userName.ifBlank { provider.displayName },
            provider = provider,
        )
        storage.save(session)
        return AuthResult.Success(session)
    }

    override fun logout() {
        storage.clear()
    }
}
