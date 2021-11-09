pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}

rootProject.name = "Pufferfish"
include("Pufferfish-API", "Pufferfish-Server")
