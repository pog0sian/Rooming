package com.example.rooming.firebase

import android.content.Context

object FirebaseTokenStore {
    private const val PREFERENCES_NAME = "rooming_firebase"
    private const val KEY_FCM_TOKEN = "fcm_token"

    fun save(context: Context, token: String) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FCM_TOKEN, token)
            .apply()
    }

    fun read(context: Context): String =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FCM_TOKEN, null)
            .orEmpty()
}
