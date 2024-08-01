pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(
            "https://s01.oss.sonatype.org/content/repositories/snapshots/"
        )
    }
}

include(":lib")
rootProject.name = "androidwrapper"