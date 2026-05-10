package com.example.rooming.firebase

import android.util.Log
import com.example.rooming.BuildConfig
import com.example.rooming.R
import com.example.rooming.feature.about.api.RemoteAppConfig
import com.example.rooming.feature.about.api.RemoteConfigService
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.ConfigUpdateListenerRegistration
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRemoteConfigService @Inject constructor() : RemoteConfigService {
    private val remoteConfig = Firebase.remoteConfig
    private val mutableConfig = MutableStateFlow(readConfig())
    private var listenerRegistration: ConfigUpdateListenerRegistration? = null
    private var isStarted = false

    override val config: StateFlow<RemoteAppConfig> = mutableConfig.asStateFlow()

    override fun start() {
        if (isStarted) return
        isStarted = true

        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
            },
        )
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Remote Config fetch failed", task.exception)
                }
                publishConfig()
            }

        listenerRegistration = remoteConfig.addOnConfigUpdateListener(
            object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    Log.d(TAG, "Remote Config updated keys: ${configUpdate.updatedKeys}")
                    remoteConfig.activate().addOnCompleteListener {
                        publishConfig()
                    }
                }

                override fun onError(error: FirebaseRemoteConfigException) {
                    Log.w(TAG, "Remote Config update error: ${error.code}", error)
                }
            },
        )
    }

    private fun publishConfig() {
        mutableConfig.value = readConfig()
    }

    private fun readConfig(): RemoteAppConfig = RemoteAppConfig(
        welcomeMessage = remoteConfig.getString(KEY_WELCOME_MESSAGE)
            .ifBlank { "Добро пожаловать в Rooming!" },
        isExperimentalEnabled = remoteConfig.getBoolean(KEY_IS_EXPERIMENTAL_ENABLED),
    )

    private companion object {
        const val TAG = "FirebaseRemoteConfig"
        const val KEY_WELCOME_MESSAGE = "welcome_message"
        const val KEY_IS_EXPERIMENTAL_ENABLED = "is_experimental_enabled"
    }
}
