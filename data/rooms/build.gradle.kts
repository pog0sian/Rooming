plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":domain:model"))
    implementation(project(":domain:repository"))
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)
}
