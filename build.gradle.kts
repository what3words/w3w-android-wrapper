// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.gradle.ktlint)
    alias(libs.plugins.android.library) apply false
}

allprojects {
    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
    }
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.layout.buildDirectory)
}