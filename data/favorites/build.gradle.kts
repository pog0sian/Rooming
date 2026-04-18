plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":domain:model"))
    implementation(project(":domain:repository"))
    implementation(project(":data:rooms"))
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)
}
