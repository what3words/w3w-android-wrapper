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
        maven(
            "https://s01.oss.sonatype.org/content/repositories/comwhat3words-1423"
        )
    }
}

include(":lib")
rootProject.name = "androidwrapper"