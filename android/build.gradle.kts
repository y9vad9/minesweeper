plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

val appVersion = (findProperty("app.version") as String?) ?: "1.0.0"

fun signingValue(env: String, property: String): String? =
    (System.getenv(env) ?: findProperty(property) as String?)?.takeIf { it.isNotBlank() }

val keystorePath = signingValue("ANDROID_KEYSTORE_FILE", "android.keystore.file")
val keystorePassword = signingValue("ANDROID_KEYSTORE_PASSWORD", "android.keystore.password")
val keystoreKeyAlias = signingValue("ANDROID_KEY_ALIAS", "android.key.alias")
val keystoreKeyPassword = signingValue("ANDROID_KEY_PASSWORD", "android.key.password")
val hasReleaseSigning = keystorePath != null && keystorePassword != null &&
    keystoreKeyAlias != null && keystoreKeyPassword != null

kotlin {
    jvmToolchain(21)

    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(projects.ui)
            implementation(projects.logic)
            implementation(projects.data)

            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)

            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.koin.core)
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
        }
    }
}

android {
    namespace = "com.y9vad9.minesweeper.android"
    compileSdk = libs.versions.android.compile.sdk.get().toInt()

    defaultConfig {
        applicationId = "com.y9vad9.minesweeper"
        minSdk = libs.versions.android.min.sdk.get().toInt()
        targetSdk = libs.versions.android.target.sdk.get().toInt()
        versionCode = appVersion.split('.').take(3).joinToString("").toIntOrNull() ?: 1
        versionName = appVersion
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(keystorePath!!)
                storePassword = keystorePassword
                keyAlias = keystoreKeyAlias
                keyPassword = keystoreKeyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
        }
    }

    lint {
        checkReleaseBuilds = false
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/AL2.0",
            "META-INF/LGPL2.1",
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE.md",
            "META-INF/LICENSE-notice.md",
        )
    }
}
