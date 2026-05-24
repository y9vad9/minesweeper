plugins {
    `kotlin-dsl`
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.gradle.kotlin.multiplatform)
    implementation(libs.gradle.kotlin.compose.compiler)
    implementation(libs.gradle.compose.multiplatform)
    implementation(libs.gradle.kotlin.serialization)
    implementation(libs.gradle.sqldelight)
    implementation(libs.gradle.kover)
    implementation(libs.gradle.detekt)
    implementation(libs.gradle.android)
    implementation(files((libs).javaClass.superclass.protectionDomain.codeSource.location))
}
