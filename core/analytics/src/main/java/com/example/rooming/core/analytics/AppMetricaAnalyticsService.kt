package com.example.rooming.core.analytics

import io.appmetrica.analytics.AppMetrica
import javax.inject.Inject

class AppMetricaAnalyticsService @Inject constructor() : AnalyticsService {
    override fun trackEvent(name: String, params: Map<String, Any>) {
        AppMetrica.reportEvent(name, params)
    }

    override fun trackError(message: String, error: Throwable?) {
        if (error == null) {
            AppMetrica.reportError(message, message)
        } else {
            AppMetrica.reportError(message, error)
        }
    }
}
