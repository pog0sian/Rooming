package com.example.rooming.feature.auth.impl

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.rooming.feature.auth.api.AuthProvider
import com.example.rooming.feature.auth.api.AuthSession
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureAuthStorage @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        "rooming_secure_auth",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun save(session: AuthSession) {
        preferences.edit()
            .putString(KEY_TOKEN, session.token)
            .putString(KEY_USER_NAME, session.userName)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_PROVIDER, session.provider.name)
            .apply()
    }

    fun read(): AuthSession? {
        val token = preferences.getString(KEY_TOKEN, null)?.takeIf(String::isNotBlank) ?: return null
        val userName = preferences.getString(KEY_USER_NAME, null)?.takeIf(String::isNotBlank) ?: return null
        val email = preferences.getString(KEY_EMAIL, null).orEmpty()
        val providerName = preferences.getString(KEY_PROVIDER, null) ?: return null
        val provider = runCatching { AuthProvider.valueOf(providerName) }.getOrNull() ?: return null

        return AuthSession(
            token = token,
            userName = userName,
            email = email,
            provider = provider,
        )
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val KEY_TOKEN = "token"
        const val KEY_USER_NAME = "user_name"
        const val KEY_EMAIL = "email"
        const val KEY_PROVIDER = "provider"
    }
}
