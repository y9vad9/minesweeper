import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("kmp-library")
    id("com.android.library")
}

private val versions = the<VersionCatalogsExtension>().named("libs")
private fun lib(name: String) =
    versions.findVersion(name).orElseThrow { GradleException("Missing version: $name") }.requiredVersion

android {
    compileSdk = lib("android-compile-sdk").toInt()
    defaultConfig {
        minSdk = lib("android-min-sdk").toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}
