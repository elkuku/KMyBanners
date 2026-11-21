import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

/** From local.properties, else a Gradle property (-Prelease.storeFile=… in the release workflow). */
fun localProperty(name: String, default: String = ""): String =
    localProperties.getProperty(name) ?: providers.gradleProperty(name).orNull ?: default

android {
    namespace = "dev.elkuku.kmybanners"
    // 37.2: the Compose libraries need at least 37.1 (targetSdk below is the behaviour the app opts in to)
    compileSdk {
        version = release(37) { minorApiLevel = 2 }
    }

    defaultConfig {
        applicationId = "dev.elkuku.kmybanners"
        minSdk = 26
        targetSdk = 37
        versionCode = 3
        versionName = "0.3.0"

        // The GitHub OAuth App (device flow, no client secret): its client id is public
        buildConfigField(
            "String", "GITHUB_CLIENT_ID",
            "\"${providers.gradleProperty("github.clientId").getOrElse("Ov23lif5FOXCzfSGElyt")}\"",
        )
    }

    signingConfigs {
        // The release key (release.* in local.properties or from the workflow); without it assembleRelease is unsigned
        if (localProperty("release.storeFile").isNotBlank()) {
            create("release") {
                storeFile = file(localProperty("release.storeFile"))
                storePassword = localProperty("release.storePassword")
                keyAlias = localProperty("release.keyAlias")
                keyPassword = localProperty("release.keyPassword")
            }
        }
        // The dev key (dev.* in local.properties or from the Pages workflow); without it assembleDev is unsigned
        if (localProperty("dev.storeFile").isNotBlank()) {
            create("dev") {
                storeFile = file(localProperty("dev.storeFile"))
                storePassword = localProperty("dev.storePassword")
                keyAlias = localProperty("dev.keyAlias")
                keyPassword = localProperty("dev.keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // The dev build ("KMyBanners Dev"): installs next to the release app, marked DEV, with the
            // language and light/dark switches in its settings
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("boolean", "DEV", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
            buildConfigField("boolean", "DEV", "false")
        }
        // The published dev build: a release build like the debug one ("KMyBanners Dev", .dev, DEV switches),
        // signed with the dev key; on GitHub Pages as kmybanners-dev.apk, built on every push to main
        create("dev") {
            initWith(getByName("release"))
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev" + localProperty("dev.build").let { if (it.isBlank()) "" else "+$it" }
            buildConfigField("boolean", "DEV", "true")
            signingConfig = signingConfigs.findByName("dev")
            matchingFallbacks += "release"
        }
    }

    // The dev build's name ("KMyBanners Dev") is the debug build's
    sourceSets["dev"].res.directories += "src/debug/res"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // English, German, Spanish for Android 13+'s per-app language setting (res/resources.properties: English is the default)
    androidResources {
        generateLocaleConfig = true
    }

    // An app bundle keeps every language: the dev build switches them itself
    bundle {
        language {
            enableSplit = false
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)

    // Medal and banner pictures on the review screen
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
}
