import java.net.*

plugins {
    alias(libs.plugins.android.library)
    id("com.avast.gradle.docker-compose") version "0.17.21"
    alias(libs.plugins.sonarqube)
    id("maven-publish")
    id("signing")
    alias(libs.plugins.dokka)
}

apply(from = "../jacoco.gradle")
apply(from = "../sonarqube.gradle")

group = "com.what3words"

/**
 * IS_SNAPSHOT_RELEASE property will be automatically added to the root gradle.properties file by the CI pipeline, depending on the GitHub branch.
 * A snapshot release is generated for every pull request merged or commit made into an epic branch.
 */
val isSnapshotRelease = findProperty("IS_SNAPSHOT_RELEASE") == "true"
version =
    if (isSnapshotRelease) "${findProperty("LIBRARY_VERSION")}-SNAPSHOT" else "${findProperty("LIBRARY_VERSION")}"

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "LIBRARY_VERSION", "\"${findProperty("LIBRARY_VERSION")}\"")
        buildConfigField(
            "String",
            "BASE_TEXT_API_ENDPOINT",
            "\"${findProperty("BASE_TEXT_API_ENDPOINT")}\""
        )
        buildConfigField("String", "TEXT_API_VERSION", "\"${findProperty("TEXT_API_VERSION")}\"")
        buildConfigField(
            "String",
            "BASE_VOICE_API_ENDPOINT",
            "\"${findProperty("BASE_VOICE_API_ENDPOINT")}\""
        )
        buildConfigField("String", "VOICE_API_VERSION", "\"${findProperty("VOICE_API_VERSION")}\"")
        buildConfigField("String", "PRE_PROD_API_URL", "\"${findProperty("W3W_PRE_PROD_URL")}\"")
        buildConfigField("String", "PRE_PROD_API_KEY", "\"${findProperty("PRE_PROD_API_KEY")}\"")

        // for robolectric
        testOptions.unitTests.apply {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmToolchain.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmToolchain.get())
    }

    buildTypes {
        named("debug") {
            enableUnitTestCoverage = false
        }
        named("release") {
            isMinifyEnabled = false
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
        }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
    namespace = "com.what3words.androidwrapper"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.core.ktx)
    // kotlin
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.kotlinx.coroutines.test)

    // w3w java wrapper
    api(libs.w3w.java.wrapper)

    // w3w core library
    api(libs.w3w.core.multiplatform)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converterGson)
    implementation(libs.retrofit.converterMoshi)
    implementation(libs.retrofit.kotlinCoroutinesAdapter)


    // testing
    testImplementation(libs.junit)
    testRuntimeOnly(libs.androidx.test.core)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.org.json)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)

    // Moshi
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    annotationProcessor(libs.moshi.kotlin.codegen)
}

//region publishing

val ossrhUsername = findProperty("OSSRH_USERNAME") as String?
val ossrhPassword = findProperty("OSSRH_PASSWORD") as String?
val signingKey = findProperty("SIGNING_KEY") as String?
val signingKeyPwd = findProperty("SIGNING_KEY_PWD") as String?

publishing {
    repositories {
        maven {
            name = "sonatype"
            val releasesRepoUrl =
                "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl =
                "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = if (version.toString()
                    .endsWith("SNAPSHOT")
            ) URI.create(snapshotsRepoUrl) else URI.create(releasesRepoUrl)

            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
        }
        publications {
            create<MavenPublication>("Maven") {
                artifactId = "w3w-android-wrapper"
                groupId = "com.what3words"
                version = project.version.toString()
                afterEvaluate {
                    from(components["release"])
                }
            }
            withType(MavenPublication::class.java) {
                val publicationName = name
                val dokkaJar =
                    project.tasks.register("${publicationName}DokkaJar", Jar::class) {
                        group = JavaBasePlugin.DOCUMENTATION_GROUP
                        description = "Assembles Kotlin docs with Dokka into a Javadoc jar"
                        archiveClassifier.set("javadoc")
                        from(tasks.named("dokkaGeneratePublicationHtml"))

                        // Each archive name should be distinct, to avoid implicit dependency issues.
                        // We use the same format as the sources Jar tasks.
                        // https://youtrack.jetbrains.com/issue/KT-46466
                        archiveBaseName.set("${archiveBaseName.get()}-$publicationName")
                    }
                artifact(dokkaJar)
                pom {
                    name.set("w3w-android-wrapper")
                    description.set("Android library for what3words REST API.")
                    url.set("https://github.com/what3words/w3w-android-wrapper")
                    licenses {
                        license {
                            name.set("The MIT License (MIT)")
                            url.set("https://github.com/what3words/w3w-android-wrapper/blob/master/LICENSE")
                        }
                    }
                    developers {
                        developer {
                            id.set("what3words")
                            name.set("what3words")
                            email.set("development@what3words.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/what3words/w3w-android-wrapper.git")
                        developerConnection.set("scm:git:ssh://git@github.com:what3words/w3w-android-wrapper.git")
                        url.set("https://github.com/what3words/w3w-android-wrapper/tree/master")
                    }
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(signingKey, signingKeyPwd)
    sign(publishing.publications)
}

//endregion