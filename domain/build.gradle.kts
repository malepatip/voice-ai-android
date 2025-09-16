plugins {
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.extra["coroutines_version"]}")

    // Dependency Injection (Pure Kotlin - no Android Hilt)
    implementation("javax.inject:javax.inject:1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${rootProject.extra["coroutines_version"]}")
    testImplementation("org.mockito:mockito-core:5.6.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
}