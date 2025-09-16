plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.voiceai.data"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${rootProject.extra["coroutines_version"]}")

    // Hilt
    implementation("com.google.dagger:hilt-android:${rootProject.extra["hilt_version"]}")
    kapt("com.google.dagger:hilt-compiler:${rootProject.extra["hilt_version"]}")

    // Room Database
    implementation("androidx.room:room-runtime:${rootProject.extra["room_version"]}")
    implementation("androidx.room:room-ktx:${rootProject.extra["room_version"]}")
    kapt("androidx.room:room-compiler:${rootProject.extra["room_version"]}")

    // Network
    implementation("com.squareup.retrofit2:retrofit:${rootProject.extra["retrofit_version"]}")
    implementation("com.squareup.retrofit2:converter-gson:${rootProject.extra["retrofit_version"]}")
    implementation("com.squareup.okhttp3:okhttp:${rootProject.extra["okhttp_version"]}")
    implementation("com.squareup.okhttp3:logging-interceptor:${rootProject.extra["okhttp_version"]}")

    // WebSocket for Hume AI
    implementation("com.squareup.okhttp3:okhttp:${rootProject.extra["okhttp_version"]}")

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${rootProject.extra["coroutines_version"]}")
    testImplementation("androidx.room:room-testing:${rootProject.extra["room_version"]}")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}

kapt {
    correctErrorTypes = true
}