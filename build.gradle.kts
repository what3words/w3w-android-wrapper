// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("javax.activation:javax.activation-api:1.2.0")
    }
}

plugins {
    alias(libs.plugins.gradle.ktlint)
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jreleaser) apply false
}

allprojects {
    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
    }
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.layout.buildDirectory)
}
