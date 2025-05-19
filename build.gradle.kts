plugins {
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
}

group = "ai.koog.kooging.book"
version = "0.0.1"

allprojects {
    repositories {
        mavenCentral()
        maven(url = "https://packages.jetbrains.team/maven/p/konfy/maven")
        maven(url = "https://packages.jetbrains.team/maven/p/grazi/grazie-platform-public")
        mavenLocal() // TODO remove after merge of latest changes to master
    }
}
