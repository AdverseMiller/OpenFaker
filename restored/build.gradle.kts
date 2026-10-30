plugins {
    id("com.google.devtools.ksp")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.androidfaker.restored"
    compileSdk = 37
    defaultConfig { minSdk = 27 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

dependencies {
    implementation("com.materialkolor:material-kolor:4.1.1")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("io.github.libxposed:service:101.0.0")
    compileOnly("io.github.libxposed:api:101.0.1")
    implementation("com.github.topjohnwu.libsu:core:6.0.0")
    implementation("com.github.topjohnwu.libsu:io:6.0.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("com.tencent:mmkv:2.3.0")
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    implementation("androidx.compose.ui:ui:1.11.4")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
}

ksp { arg("room.schemaLocation", layout.buildDirectory.dir("generated/roomSchemas").get().asFile.path) }
