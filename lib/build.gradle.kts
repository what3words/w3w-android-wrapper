import java.net.*

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.avast.gradle.docker-compose") version "0.14.3"
    id("org.sonarqube")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "1.5.0"
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
    compileSdk = 34

    kotlinOptions {
        jvmTarget = "1.8"
    }

    defaultConfig {
        minSdk = 19

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
    implementation("androidx.core:core-ktx:1.13.1")
    // kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

    // w3w java wrapper
    api("com.what3words:w3w-java-wrapper:3.1.21")

    // w3w core library
    api("com.what3words:w3w-core-android:1.0.0")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.5.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")


    // testing
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("androidx.test:core:1.6.1")
    testImplementation("com.google.truth:truth:1.4.2")
    testImplementation("io.mockk:mockk:1.12.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("org.json:json:20230618")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Moshi
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    annotationProcessor("com.squareup.moshi:moshi-kotlin-codegen:1.8.0")
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
                        from(tasks.named("dokkaHtml"))

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