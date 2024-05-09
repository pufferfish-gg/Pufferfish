pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://plugins.gradle.org/m2/")
    }
}

rootProject.name = "pufferfish"
include("pufferfish-api", "pufferfish-server")
