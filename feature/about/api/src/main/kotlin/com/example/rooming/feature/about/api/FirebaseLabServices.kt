package com.example.rooming.feature.about.api

import kotlinx.coroutines.flow.StateFlow

data class RemoteAppConfig(
    val welcomeMessage: String = "Добро пожаловать в Rooming!",
    val isExperimentalEnabled: Boolean = false,
)

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val fcmToken: String = "",
    val updatedAt: String = "",
)

interface RemoteConfigService {
    val config: StateFlow<RemoteAppConfig>

    fun start()
}

interface UserProfileService {
    val profile: StateFlow<UserProfile?>
    val errorMessage: StateFlow<String?>

    fun start()
}
