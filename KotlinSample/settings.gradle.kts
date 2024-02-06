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

        // Equativ maven repository
        maven(url = "https://packagecloud.io/smartadserver/android/maven2")
    }
}

rootProject.name = "KotlinSample"
include(":app")
