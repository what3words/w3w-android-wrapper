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
        mavenLocal()
        maven(url = "https://central.sonatype.com/repository/maven-snapshots/")
    }
    versionCatalogs {
        create("libs") {
            from("com.what3words:android-version-catalog:2026.06.01")

            // minSdk differs from the remote catalog (25); pin to this project's value.
            version("minSdk", "21")

            // Libraries not present in the remote catalog.
            library("w3w-java-wrapper", "com.what3words:w3w-java-wrapper:3.1.22")
        }
    }
}

include(":lib")
rootProject.name = "androidwrapper"
