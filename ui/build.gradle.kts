plugins {
    id("kmp-web-library")
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

val composeDesktopCurrentOs = run {
    val osName = System.getProperty("os.name").orEmpty().lowercase()
    val osArch = System.getProperty("os.arch").orEmpty().lowercase()
    when {
        osName.startsWith("mac") && (osArch == "aarch64" || osArch == "arm64") -> libs.compose.desktop.macos.arm64
        osName.startsWith("mac") -> libs.compose.desktop.macos.x64
        osName.startsWith("win") -> libs.compose.desktop.windows.x64
        osArch == "aarch64" || osArch == "arm64" -> libs.compose.desktop.linux.arm64
        else -> libs.compose.desktop.linux.x64
    }
}

android {
    namespace = "com.y9vad9.minesweeper.ui"
}

kotlin {
    sourceSets {
        val chartedMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.aay.chart)
            }
        }
        jvmMain { dependsOn(chartedMain) }
        androidMain { dependsOn(chartedMain) }
        wasmJsMain { dependsOn(chartedMain) }

        commonMain.dependencies {
            implementation(projects.logic)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            api(libs.flowmvi.core)
            api(libs.flowmvi.compose)

            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.compose.ui.test)
        }
        jvmTest.dependencies {
            implementation(composeDesktopCurrentOs)
        }
    }
}
