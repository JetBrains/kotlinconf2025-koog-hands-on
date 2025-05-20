rootProject.name = "koog-book"

pluginManagement {
    resolutionStrategy {
    }

    repositories {
        gradlePluginPortal()
        maven(url = "https://packages.jetbrains.team/maven/p/grazi/grazie-platform-public")
    }
}

include(":client")
include(":server")
