plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.android1500.androidfaker"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.android1500.androidfaker.recovery"
        minSdk = 27
        targetSdk = 36
        versionCode = 20260521
        versionName = "1.0.0"
    }
    buildTypes {
        debug {
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

dependencies {
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("com.tencent:mmkv:2.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation(project(":restored"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.room:room-runtime:2.8.4")
}
