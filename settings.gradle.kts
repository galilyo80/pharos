pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Shizuku / rikka maven
        maven("https://maven.rikka.dev/releases")
        maven("https://jitpack.io")
    }
}

rootProject.name = "Pharos"
include(":app")
