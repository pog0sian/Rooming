package com.example.rooming.feature.auth.impl

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rooming.core.ui.R as CoreUiR
import com.example.rooming.core.ui.SectionCard
import com.example.rooming.feature.auth.api.AuthConfig
import com.example.rooming.feature.auth.api.AuthProvider
import com.vk.id.AccessToken
import com.vk.id.VKID
import com.vk.id.VKIDAuthFail
import com.vk.id.auth.AuthCodeData
import com.vk.id.auth.VKIDAuthCallback
import com.vk.id.auth.VKIDAuthParams
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdk

@Composable
fun LoginRoute(
    authConfig: AuthConfig,
    onLoggedIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val yandexSdk = remember(context) {
        YandexAuthSdk.create(YandexAuthOptions(context))
    }

    LaunchedEffect(Unit) {
        viewModel.onScreenViewed()
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    val yandexLauncher = rememberLauncherForActivityResult(yandexSdk.contract) { result ->
        when (result) {
            is YandexAuthResult.Success -> viewModel.onExternalLoginSuccess(
                provider = AuthProvider.YANDEX,
                token = result.token.value,
                userName = "Пользователь Яндекс",
                onLoggedIn = onLoggedIn,
            )
            is YandexAuthResult.Failure -> viewModel.onExternalLoginError(
                provider = AuthProvider.YANDEX,
                error = result.exception,
            )
            YandexAuthResult.Cancelled -> viewModel.onExternalLoginCancelled(AuthProvider.YANDEX)
        }
    }

    val vkAuthCallback = remember(viewModel, onLoggedIn) {
        object : VKIDAuthCallback {
            override fun onAuth(accessToken: AccessToken) {
                val userData = accessToken.userData
                val userName = listOfNotNull(
                    userData.firstName.takeIf(String::isNotBlank),
                    userData.lastName.takeIf(String::isNotBlank),
                ).joinToString(separator = " ").ifBlank {
                    "VK ID ${accessToken.userID}"
                }

                viewModel.onExternalLoginSuccess(
                    provider = AuthProvider.VK,
                    token = accessToken.token,
                    userName = userName,
                    onLoggedIn = onLoggedIn,
                )
            }

            override fun onAuthCode(authCodeData: AuthCodeData, isCompletion: Boolean) = Unit

            override fun onFail(fail: VKIDAuthFail) {
                if (fail is VKIDAuthFail.Canceled) {
                    viewModel.onExternalLoginCancelled(AuthProvider.VK)
                } else {
                    viewModel.onExternalLoginError(
                        provider = AuthProvider.VK,
                        error = IllegalStateException(fail.description),
                    )
                }
            }
        }
    }

    LoginScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onYandexClick = {
            if (authConfig.yandexClientId.isBlank()) {
                viewModel.onExternalLoginError(
                    provider = AuthProvider.YANDEX,
                    error = IllegalStateException("Вход через Яндекс ID пока не настроен"),
                )
            } else {
                yandexLauncher.launch(YandexAuthLoginOptions())
            }
        },
        onVkClick = {
            if (authConfig.vkClientId.isBlank() || authConfig.vkClientSecret.isBlank()) {
                viewModel.onExternalLoginError(
                    provider = AuthProvider.VK,
                    error = IllegalStateException("Вход через VK пока не настроен"),
                )
            } else {
                VKID.instance.authorize(
                    lifecycleOwner = lifecycleOwner,
                    callback = vkAuthCallback,
                    params = VKIDAuthParams {
                        scopes = emptySet()
                    },
                )
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    snackbarHostState: SnackbarHostState,
    onYandexClick: () -> Unit,
    onVkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF082B66),
                                Color(0xFF0B5FFF),
                                Color(0xFF7DB7FF),
                            ),
                        ),
                    )
                    .padding(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Image(
                        painter = painterResource(CoreUiR.drawable.door_open_24),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                    )
                    Text(
                        text = "Rooming",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = "Бронируйте аудитории спокойно и быстро",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.82f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            SectionCard(
                title = "Войти",
                subtitle = "Выберите удобный способ авторизации.",
            ) {
                Button(
                    onClick = onYandexClick,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text("Войти через Яндекс ID")
                }

                OutlinedButton(
                    onClick = onVkClick,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Войти через VK")
                }
            }
        }
    }
}
