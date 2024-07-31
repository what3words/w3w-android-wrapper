// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("org.jacoco:org.jacoco.core:0.8.7")
        classpath("com.android.tools.build:gradle:8.5.0")
    }

    extra.apply {
        set("androidx_core_version", "1.10.1")
        set("kotlinx_coroutines_version", "1.6.4")
        set("what3words_java_wrapper_version", "3.1.20")
        set("what3words_core_android_version", "1.0.0")
        set("retrofit_version", "2.9.0")
        set("junit_version", "4.13.2")
        set("androidx_test_version", "1.5.0")
        set("truth_version", "1.1.3")
        set("mockk_version", "1.12.1")
        set("android_arch_testing_version", "2.2.0")
        set("robolectric_version", "4.10.3")
        set("json_version", "20230618")
        set("androidx_junit_version", "1.1.5")
        set("espresso_version", "3.4.0")
        set("moshi_version", "1.14.0")
        set("retrofit_moshi_version", "2.5.0")
        set("retrofit_coroutines_adapter_version", "0.9.2")
        set("moshi_codegen_version", "1.8.0")
        set("mock_webserver_version", "4.12.0")
    }
}

plugins {
    id("org.jetbrains.kotlin.android") version "1.8.20" apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0" apply true
    id("org.sonarqube") version "3.3" apply false
    id("com.android.library") version "8.5.0" apply false
    id("com.autonomousapps.dependency-analysis") version "1.20.0" apply true
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}