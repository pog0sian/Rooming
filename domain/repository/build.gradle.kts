plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":domain:model"))
    implementation(libs.kotlinx.coroutines.core)
}
