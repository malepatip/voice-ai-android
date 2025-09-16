buildscript {
    ext {
        compose_version = "1.5.4"
        kotlin_version = "1.9.10"
        hilt_version = "2.48"
        lifecycle_version = "2.7.0"
        coroutines_version = "1.7.3"
        retrofit_version = "2.9.0"
        okhttp_version = "4.12.0"
        room_version = "2.6.0"
    }
}

plugins {
    id("com.android.application") version "8.1.2" apply false
    id("com.android.library") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.10" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("kotlin-parcelize") apply false
}