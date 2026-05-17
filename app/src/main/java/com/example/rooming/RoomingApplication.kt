package com.example.rooming

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.rooming.sync.RoomingSyncWorker
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vk.id.VKID
import com.yandex.mapkit.MapKitFactory
import java.util.concurrent.TimeUnit
import dagger.hilt.android.HiltAndroidApp
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

@HiltAndroidApp
class RoomingApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseCrashlytics.getInstance().setCustomKey("app_start_mode", "cold")
        scheduleBackgroundSync()

        if (BuildConfig.APPMETRICA_API_KEY.isNotBlank()) {
            val config = AppMetricaConfig
                .newConfigBuilder(BuildConfig.APPMETRICA_API_KEY)
                .withCrashReporting(true)
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

    private fun scheduleBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<RoomingSyncWorker>(
            SYNC_INTERVAL_HOURS,
            TimeUnit.HOURS,
        )
            .setConstraints(constraints)
            .addTag(SYNC_WORK_NAME)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    private companion object {
        const val SYNC_WORK_NAME = "rooming_firebase_profile_sync"
        const val SYNC_INTERVAL_HOURS = 6L
    }
}
