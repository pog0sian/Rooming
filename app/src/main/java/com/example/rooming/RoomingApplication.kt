package com.example.rooming

import android.app.Application
import com.vk.id.VKID
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

@HiltAndroidApp
class RoomingApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.APPMETRICA_API_KEY.isNotBlank()) {
            val config = AppMetricaConfig
                .newConfigBuilder(BuildConfig.APPMETRICA_API_KEY)
                .withLogs()
                .build()
            AppMetrica.activate(this, config)
            AppMetrica.enableActivityAutoTracking(this)
        }

        if (BuildConfig.YANDEX_MAPKIT_API_KEY.isNotBlank()) {
            MapKitFactory.setApiKey(BuildConfig.YANDEX_MAPKIT_API_KEY)
            MapKitFactory.initialize(this)
        }

        if (BuildConfig.VK_CLIENT_ID.isNotBlank() && BuildConfig.VK_CLIENT_SECRET.isNotBlank()) {
            VKID.init(this)
        }
    }
}
