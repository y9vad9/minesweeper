import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("kmp-library")
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

val appVersion = (findProperty("app.version") as String?) ?: "1.0.0"
val appVendor = (findProperty("app.vendor") as String?) ?: "y9vad9"
val appDescription = (findProperty("app.description") as String?) ?: "Minesweeper"
val appCopyright = (findProperty("app.copyright") as String?) ?: "Copyright (c) 2025 y9vad9"

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

kotlin {
    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xjdk-release=21")
                }
            }
        }
    }

    sourceSets {
        jvmMain.dependencies {
            implementation(projects.ui)
            implementation(projects.logic)
            implementation(projects.data)
            implementation(composeDesktopCurrentOs)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.kotlinx.coroutines.swing)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.y9vad9.minesweeper.desktop.MainKt"
        jvmArgs += listOf("-Dapple.awt.application.appearance=system")

        nativeDistributions {
            modules(
                "java.sql",
                "java.naming",
                "java.management",
                "java.logging",
                "jdk.unsupported",
            )

            packageName = "Minesweeper"
            packageVersion = appVersion
            description = appDescription
            copyright = appCopyright
            vendor = appVendor

            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Pkg,
                TargetFormat.Msi,
                TargetFormat.Exe,
                TargetFormat.Deb,
                TargetFormat.Rpm,
            )

            macOS {
                bundleID = "com.y9vad9.minesweeper"
                packageName = "Minesweeper"
                appCategory = "public.app-category.puzzle-games"
                dockName = "Minesweeper"
                val ico = layout.projectDirectory.file("icons/macos/icon.icns").asFile
                if (ico.exists()) iconFile.set(ico)
            }

            windows {
                upgradeUuid = "1f3b1c4d-2a4f-4e6c-9b1f-7d5b8e9a0c11"
                menuGroup = "Minesweeper"
                perUserInstall = true
                shortcut = true
                menu = true
                val ico = layout.projectDirectory.file("icons/windows/icon.ico").asFile
                if (ico.exists()) iconFile.set(ico)
            }

            linux {
                packageName = "minesweeper"
                debMaintainer = "y9vad9@users.noreply.github.com"
                menuGroup = "Game"
                appCategory = "Game"
                appRelease = "1"
                rpmLicenseType = "MIT"
                val ico = layout.projectDirectory.file("icons/linux/icon.png").asFile
                if (ico.exists()) iconFile.set(ico)
            }
        }

        buildTypes.release.proguard {
            isEnabled.set(false)
            obfuscate.set(false)
        }
    }
}
