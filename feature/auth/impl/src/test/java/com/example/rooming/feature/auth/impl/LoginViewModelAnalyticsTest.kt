package com.example.rooming.feature.auth.impl

import com.example.rooming.core.analytics.FakeAnalyticsService
import com.example.rooming.core.common.CrashReporter
import com.example.rooming.feature.auth.api.AuthProvider
import com.example.rooming.feature.auth.api.AuthResult
import com.example.rooming.feature.auth.api.AuthService
import com.example.rooming.feature.auth.api.AuthSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelAnalyticsTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `screen viewed sends analytics event`() {
        val analytics = FakeAnalyticsService()
        val viewModel = LoginViewModel(
            authService = FakeAuthService(),
            yandexProfileClient = FakeYandexProfileClient(),
            analytics = analytics,
            crashReporter = FakeCrashReporter(),
        )

        viewModel.onScreenViewed()

        assertEquals("screen_viewed", analytics.events.single().name)
        assertEquals("login", analytics.events.single().params["screen_name"])
    }

    @Test
    fun `successful yandex login sends user logged in analytics event`() = runTest {
        val analytics = FakeAnalyticsService()
        val viewModel = LoginViewModel(
            authService = FakeAuthService(),
            yandexProfileClient = FakeYandexProfileClient(),
            analytics = analytics,
            crashReporter = FakeCrashReporter(),
        )
        var loggedIn = false

        viewModel.onExternalLoginSuccess(
            provider = AuthProvider.YANDEX,
            token = "test-token",
            userName = "Тестовый пользователь",
            onLoggedIn = { loggedIn = true },
        )
        advanceUntilIdle()

        val event = analytics.events.single()
        assertEquals("user_logged_in", event.name)
        assertEquals("yandex", event.params["provider"])
        assertTrue(loggedIn)
    }
}

private class FakeAuthService : AuthService {
    override fun hasValidSession(): Boolean = false

    override fun getSavedSession(): AuthSession? = null

    override suspend fun completeLogin(
        provider: AuthProvider,
        token: String,
        userName: String,
        email: String,
    ): AuthResult = AuthResult.Success(
        AuthSession(
            token = token,
            userName = userName,
            email = email,
            provider = provider,
        ),
    )

    override fun logout() = Unit
}

private class FakeCrashReporter : CrashReporter {
    override fun log(message: String) = Unit

    override fun setKey(key: String, value: String) = Unit

    override fun setUserId(userId: String?) = Unit

    override fun recordNonFatal(throwable: Throwable) = Unit
}

private class FakeYandexProfileClient : YandexProfileClient() {
    override suspend fun getProfile(token: String): Result<YandexProfile> =
        Result.success(YandexProfile(name = "Тестовый пользователь", email = "test@yandex.ru"))
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
