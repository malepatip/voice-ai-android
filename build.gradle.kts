// Top-level build file for Voice AI Android
buildscript {
    extra.apply {
        set("compose_version", "1.5.3")
        set("kotlin_version", "1.9.10")
        set("hilt_version", "2.48")
        set("lifecycle_version", "2.7.0")
        set("coroutines_version", "1.7.3")
        set("retrofit_version", "2.9.0")
        set("okhttp_version", "4.12.0")
        set("room_version", "2.6.0")
    }
}

plugins {
    id("com.android.application") version "8.1.2" apply false
    id("com.android.library") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.10" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
