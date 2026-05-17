import java.util.Properties
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.firebase.crashlytics)
    id("com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun localProperty(name: String): String = localProperties.getProperty(name).orEmpty()

android {
    namespace = "com.example.rooming"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.rooming"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "APPMETRICA_API_KEY", "\"${localProperty("APPMETRICA_API_KEY")}\"")
        buildConfigField("String", "YANDEX_CLIENT_ID", "\"${localProperty("YANDEX_CLIENT_ID")}\"")
        buildConfigField("String", "VK_CLIENT_ID", "\"${localProperty("VK_CLIENT_ID")}\"")
        buildConfigField("String", "VK_CLIENT_SECRET", "\"${localProperty("VK_CLIENT_SECRET")}\"")
        buildConfigField("String", "YANDEX_MAPKIT_API_KEY", "\"${localProperty("YANDEX_MAPKIT_API_KEY")}\"")

        manifestPlaceholders["YANDEX_CLIENT_ID"] = localProperty("YANDEX_CLIENT_ID")
        manifestPlaceholders["VKIDClientID"] = localProperty("VK_CLIENT_ID")
        manifestPlaceholders["VKIDClientSecret"] = localProperty("VK_CLIENT_SECRET")
        manifestPlaceholders["VKIDRedirectHost"] = "vk.ru"
        manifestPlaceholders["VKIDRedirectScheme"] = "vk${localProperty("VK_CLIENT_ID")}"
    }

    buildTypes {
        debug {
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = true
            }
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("demo") {
            dimension = "environment"
            buildConfigField("String", "ROOMING_ENV", "\"demo\"")
            buildConfigField("Boolean", "LAB_TOOLS_ENABLED", "true")
        }

        create("prod") {
            dimension = "environment"
            buildConfigField("String", "ROOMING_ENV", "\"prod\"")
            buildConfigField("Boolean", "LAB_TOOLS_ENABLED", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:analytics"))
    implementation(project(":core:navigation"))
    implementation(project(":core:ui"))
    implementation(project(":domain:repository"))
    implementation(project(":data:rooms"))
    implementation(project(":data:favorites"))
    implementation(project(":data:bookings"))
    implementation(project(":feature:rooms:api"))
    implementation(project(":feature:rooms:impl"))
    implementation(project(":feature:favorites:api"))
    implementation(project(":feature:favorites:impl"))
    implementation(project(":feature:bookings:api"))
    implementation(project(":feature:bookings:impl"))
    implementation(project(":feature:auth:api"))
    implementation(project(":feature:auth:impl"))
    implementation(project(":feature:about:api"))
    implementation(project(":feature:about:impl"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.config)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.appmetrica.analytics)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.google.hilt.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.vkid)
    implementation(libs.yandex.mapkit.lite)

    kapt(libs.google.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
