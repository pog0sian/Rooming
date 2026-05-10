package com.example.rooming.firebase

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.rooming.MainActivity
import com.example.rooming.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token: $token")
        FirebaseTokenStore.save(this, token)
        updateTokenInFirestore(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        showNotification(message)
    }

    private fun updateTokenInFirestore(token: String) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUserToken(currentUser.uid, token)
            return
        }

        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                result.user?.uid?.let { userId -> updateUserToken(userId, token) }
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Failed to sign in anonymously for FCM token update", error)
            }
    }

    private fun updateUserToken(userId: String, token: String) {
        FirebaseFirestore.getInstance()
            .collection(USERS_COLLECTION)
            .document(userId)
            .set(
                mapOf(
                    FIELD_FCM_TOKEN to token,
                    FIELD_UPDATED_AT to System.currentTimeMillis(),
                ),
                SetOptions.merge(),
            )
            .addOnFailureListener { error ->
                Log.w(TAG, "Failed to update FCM token in Firestore", error)
            }
    }

    private fun showNotification(message: RemoteMessage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Notification skipped: POST_NOTIFICATIONS is not granted")
            return
        }

        createNotificationChannel()

        val data = message.data
        val roomId = data[KEY_ROOM_ID].orEmpty().ifBlank { DEFAULT_ROOM_ID }
        val title = message.notification?.title ?: data[KEY_TITLE] ?: "Rooming"
        val body = message.notification?.body ?: data[KEY_BODY] ?: "Откройте аудиторию из уведомления"
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_ROOM_ID, roomId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            roomId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.door_open_24)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this).notify(roomId.hashCode(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Rooming notifications",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Уведомления о бронировании и аудиториях"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val TAG = "PushMessagingService"
        const val CHANNEL_ID = "rooming_updates"
        const val DEFAULT_ROOM_ID = "room-101"
        const val KEY_ROOM_ID = "roomId"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val USERS_COLLECTION = "users"
        const val FIELD_FCM_TOKEN = "fcmToken"
        const val FIELD_UPDATED_AT = "updatedAt"
    }
}
