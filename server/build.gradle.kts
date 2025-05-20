group = rootProject.group
version = rootProject.version

plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.logback.classic)

    implementation(libs.koog.agents)
    implementation("io.ktor:ktor-server-sse:3.1.3")

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}
