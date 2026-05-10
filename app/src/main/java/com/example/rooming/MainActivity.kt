package com.example.rooming

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.rooming.core.navigation.FeatureEntry
import com.example.rooming.core.ui.RoomingTheme
import com.example.rooming.feature.auth.api.AuthService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var notificationRoomId by mutableStateOf<String?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        // The app still works when notifications are denied; FCM data is logged and stored.
    }

    @Inject
    lateinit var featureEntries: Set<@JvmSuppressWildcards FeatureEntry>

    @Inject
    lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationRoomId = intent.getStringExtra(EXTRA_ROOM_ID)
        requestNotificationPermissionIfNeeded()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.rgb(244, 248, 255),
                darkScrim = Color.rgb(244, 248, 255),
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.rgb(244, 248, 255),
                darkScrim = Color.rgb(244, 248, 255),
            ),
        )
        setContent {
            RoomingTheme {
                RoomingApp(
                    featureEntries = featureEntries.toList(),
                    hasAuthorizedSession = authService.hasValidSession(),
                    notificationRoomId = notificationRoomId,
                    onNotificationHandled = { notificationRoomId = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationRoomId = intent.getStringExtra(EXTRA_ROOM_ID)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val isGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        const val EXTRA_ROOM_ID = "extra_room_id"
    }
}
