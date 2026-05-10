package com.example.rooming.core.analytics

import android.util.Log

class FakeAnalyticsService : AnalyticsService {
    val events = mutableListOf<AnalyticsEvent>()
    val errors = mutableListOf<AnalyticsError>()

    override fun trackEvent(name: String, params: Map<String, Any>) {
        events += AnalyticsEvent(name = name, params = params)
        logDebug("event=$name params=$params")
    }

    override fun trackError(message: String, error: Throwable?) {
        errors += AnalyticsError(message = message, error = error)
        logDebug("error=$message throwable=${error?.message}")
    }

    private fun logDebug(message: String) {
        runCatching {
            Log.d("FakeAnalytics", message)
        }.onFailure {
            println("FakeAnalytics: $message")
        }
    }
}

data class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any>,
)

data class AnalyticsError(
    val message: String,
    val error: Throwable?,
)
