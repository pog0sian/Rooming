package com.example.rooming.firebase

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import com.example.rooming.feature.about.api.UserProfile
import com.example.rooming.feature.about.api.UserProfileService
import com.example.rooming.feature.auth.api.AuthService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseUserProfileService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authService: AuthService,
) : UserProfileService {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val mutableProfile = MutableStateFlow<UserProfile?>(null)
    private val mutableErrorMessage = MutableStateFlow<String?>(null)
    private var listenerRegistration: ListenerRegistration? = null
    private var isStarted = false

    override val profile: StateFlow<UserProfile?> = mutableProfile.asStateFlow()
    override val errorMessage: StateFlow<String?> = mutableErrorMessage.asStateFlow()

    override fun start() {
        if (isStarted) return
        isStarted = true

        val currentUser = auth.currentUser
        if (currentUser != null) {
            syncProfile(currentUser.uid)
            return
        }

        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                result.user?.uid?.let(::syncProfile)
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Anonymous Firebase Auth failed", error)
                mutableErrorMessage.value = "Не удалось авторизоваться в Firebase"
            }
    }

    private fun syncProfile(userId: String) {
        val session = authService.getSavedSession()
        val token = FirebaseTokenStore.read(context)
        val name = session?.userName.orEmpty().ifBlank { "Пользователь Rooming" }
        val email = session?.provider
            ?.let { provider -> "${provider.analyticsName}@rooming.local" }
            ?: "user@rooming.local"

        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .set(
                mapOf(
                    FIELD_NAME to name,
                    FIELD_EMAIL to email,
                    FIELD_FCM_TOKEN to token,
                    FIELD_UPDATED_AT to System.currentTimeMillis(),
                ),
                SetOptions.merge(),
            )
            .addOnFailureListener { error ->
                Log.w(TAG, "Failed to save user profile", error)
                mutableErrorMessage.value = "Не удалось сохранить профиль в Firestore"
            }

        requestCurrentFcmToken(userId)

        listenerRegistration?.remove()
        listenerRegistration = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Profile listener failed", error)
                    mutableErrorMessage.value = "Firestore вернул ошибку: ${error.code}"
                    return@addSnapshotListener
                }

                mutableErrorMessage.value = null
                mutableProfile.value = UserProfile(
                    userId = userId,
                    name = snapshot?.getString(FIELD_NAME).orEmpty(),
                    email = snapshot?.getString(FIELD_EMAIL).orEmpty(),
                    fcmToken = snapshot?.getString(FIELD_FCM_TOKEN).orEmpty(),
                    updatedAt = snapshot?.getLong(FIELD_UPDATED_AT)
                        ?.let(::formatUpdatedAt)
                        .orEmpty(),
                )
            }
    }

    private fun requestCurrentFcmToken(userId: String) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d(TAG, "Current FCM token: $token")
                FirebaseTokenStore.save(context, token)
                firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .set(
                        mapOf(
                            FIELD_FCM_TOKEN to token,
                            FIELD_UPDATED_AT to System.currentTimeMillis(),
                        ),
                        SetOptions.merge(),
                    )
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Failed to get current FCM token", error)
            }
    }

    private fun formatUpdatedAt(timestamp: Long): String =
        DateFormat.format("dd.MM.yyyy HH:mm", Date(timestamp)).toString()

    private companion object {
        const val TAG = "FirebaseUserProfile"
        const val USERS_COLLECTION = "users"
        const val FIELD_NAME = "name"
        const val FIELD_EMAIL = "email"
        const val FIELD_FCM_TOKEN = "fcmToken"
        const val FIELD_UPDATED_AT = "updatedAt"
    }
}
