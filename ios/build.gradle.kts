import org.gradle.api.tasks.Exec
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    id("code-quality")
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Minesweeper"
            isStatic = true
            export(projects.ui)
            export(projects.logic)
            export(projects.data)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.ui)
            api(projects.logic)
            api(projects.data)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
    }
}

val iosAppDir = rootProject.layout.projectDirectory.dir("iosApp")
val iosOutDir = layout.buildDirectory.dir("ios")
val iosArchivePath = iosOutDir.map { it.file("Minesweeper.xcarchive").asFile.absolutePath }
val ipaExportDir = iosOutDir.map { it.dir("ipa").asFile.absolutePath }

val iosTeamId: String? = findProperty("ios.teamId") as String?
val iosExportMethod: String = (findProperty("ios.exportMethod") as String?) ?: "development"

fun discoverTeamIdFromKeychain(): String? = runCatching {
    val proc = ProcessBuilder("security", "find-identity", "-v", "-p", "codesigning")
        .redirectErrorStream(true).start()
    proc.waitFor(5, TimeUnit.SECONDS)
    val text = proc.inputStream.bufferedReader().readText()
    Regex("""Apple Development[^()]*\(([A-Z0-9]{10})\)""").find(text)?.groupValues?.get(1)
}.getOrNull()
val iosBundleId: String = (findProperty("ios.bundleId") as String?) ?: "com.y9vad9.minesweeper"
val iosVersion: String = (findProperty("ios.version") as String?)
    ?: (findProperty("app.version") as String?) ?: "1.0.0"
val iosBuildNumber: String = (findProperty("ios.buildNumber") as String?) ?: "1"
val iosConfiguration: String = (findProperty("ios.configuration") as String?) ?: "Release"

tasks.register<Exec>("iosArchive") {
    group = "ios"
    description = "Runs `xcodebuild archive` for the iosApp Xcode project; the embedded " +
        "Run Script phase invokes :ios:embedAndSignAppleFrameworkForXcode."

    workingDir(iosAppDir)
    commandLine("xcodebuild", "-version")

    doFirst {
        mkdir(iosOutDir.get().asFile)
        val team = iosTeamId ?: discoverTeamIdFromKeychain()
        val args = mutableListOf(
            "xcodebuild",
            "-project", "iosApp.xcodeproj",
            "-scheme", "iosApp",
            "-configuration", iosConfiguration,
            "-destination", "generic/platform=iOS",
            "-archivePath", iosArchivePath.get(),
            "PRODUCT_BUNDLE_IDENTIFIER=$iosBundleId",
            "MARKETING_VERSION=$iosVersion",
            "CURRENT_PROJECT_VERSION=$iosBuildNumber",
        )
        if (team != null) {
            args += "DEVELOPMENT_TEAM=$team"
            args += "-allowProvisioningUpdates"
            logger.lifecycle("iosArchive: signing with team $team")
        } else {
            args += listOf(
                "CODE_SIGNING_ALLOWED=NO",
                "CODE_SIGNING_REQUIRED=NO",
                "CODE_SIGN_IDENTITY=",
                "CODE_SIGN_ENTITLEMENTS=",
                "EXPANDED_CODE_SIGN_IDENTITY=",
            )
            logger.lifecycle("iosArchive: no team id found; building unsigned")
        }
        args += "archive"
        commandLine(args)
    }
}

abstract class ExportIpaTask @Inject constructor(
    private val execOps: ExecOperations,
) : DefaultTask() {
    @get:Input abstract val archivePath: Property<String>
    @get:Input abstract val outputDir: Property<String>
    @get:Input abstract val iosAppDirPath: Property<String>
    @get:Input @get:Optional abstract val teamId: Property<String>
    @get:Input abstract val exportMethod: Property<String>
    @get:Input abstract val outRoot: Property<String>

    @TaskAction
    fun run() {
        val outDir = File(outputDir.get())
        outDir.deleteRecursively()
        outDir.mkdirs()
        val archive = File(archivePath.get())
        val team = teamId.orNull ?: runCatching {
            val proc = ProcessBuilder("security", "find-identity", "-v", "-p", "codesigning")
                .redirectErrorStream(true).start()
            proc.waitFor(5, TimeUnit.SECONDS)
            val out = proc.inputStream.bufferedReader().readText()
            Regex("""Apple Development[^()]*\(([A-Z0-9]{10})\)""").find(out)?.groupValues?.get(1)
        }.getOrNull()
        val appDir = File(iosAppDirPath.get())

        if (team != null) {
            val templateName = when (exportMethod.get()) {
                "app-store", "appstore" -> "ExportOptions-appstore.plist"
                else -> "ExportOptions-development.plist"
            }
            val template = File(appDir, templateName).readText()
            val populated = template.replace(
                "</dict>\n</plist>",
                "    <key>teamID</key>\n    <string>$team</string>\n</dict>\n</plist>",
            )
            val optionsFile = File(outRoot.get(), "ExportOptions.plist")
            optionsFile.parentFile.mkdirs()
            optionsFile.writeText(populated)
            execOps.exec {
                workingDir = appDir
                commandLine(
                    "xcodebuild", "-exportArchive",
                    "-archivePath", archive.absolutePath,
                    "-exportPath", outDir.absolutePath,
                    "-exportOptionsPlist", optionsFile.absolutePath,
                )
            }
        } else {
            val appsDir = File(archive, "Products/Applications")
            val app = appsDir.listFiles()?.firstOrNull { it.name.endsWith(".app") }
                ?: error("No .app inside ${appsDir.absolutePath}")
            val payload = File(outDir, "Payload")
            payload.mkdirs()
            execOps.exec { commandLine("cp", "-R", app.absolutePath, payload.absolutePath) }
            val ipa = File(outDir, "iosApp.ipa")
            execOps.exec {
                workingDir = outDir
                commandLine("zip", "-qry", ipa.absolutePath, "Payload")
            }
            payload.deleteRecursively()
            logger.lifecycle("Unsigned .ipa written to: ${ipa.absolutePath}")
            logger.lifecycle("(pass -Pios.teamId=<TEAM_ID> for a properly signed export)")
        }
    }
}

tasks.register<ExportIpaTask>("iosExportIpa") {
    group = "ios"
    description = "Exports the archive as an .ipa. Uses xcodebuild -exportArchive when " +
        "ios.teamId is set; otherwise manually repacks the .app into Payload/."
    dependsOn("iosArchive")
    archivePath.set(iosArchivePath)
    outputDir.set(ipaExportDir)
    iosAppDirPath.set(iosAppDir.asFile.absolutePath)
    outRoot.set(iosOutDir.map { it.asFile.absolutePath })
    if (iosTeamId != null) teamId.set(iosTeamId)
    exportMethod.set(iosExportMethod)
}

tasks.register<Exec>("bootstrapIos") {
    group = "ios"
    description = "One-shot post-Xcode-install bootstrap: accepts the Xcode license and " +
        "installs the additional components Xcode normally prompts for on first launch. " +
        "Requires sudo. Skips entirely if `xcode-select -p` doesn't point at Xcode.app."
    commandLine(
        "sh", "-c",
        """
        set -e
        if ! xcode-select -p | grep -q "Xcode.app"; then
          echo "xcode-select is not pointing at Xcode.app:" 1>&2
          echo "  current: \$(xcode-select -p)" 1>&2
          echo "  run: sudo xcode-select -s /Applications/Xcode.app/Contents/Developer" 1>&2
          exit 1
        fi
        sudo xcodebuild -license accept
        sudo xcodebuild -runFirstLaunch
        echo "Done. Next step: open Xcode once, Settings → Accounts → + → sign in."
        """.trimIndent(),
    )
}

tasks.register("buildIpa") {
    group = "ios"
    description = "End-to-end: builds the Kotlin framework, archives the Xcode project, " +
        "and exports an .ipa under build/ios/ipa/."
    dependsOn("iosExportIpa")
}

abstract class InstallOnDeviceTask @Inject constructor(
    private val execOps: ExecOperations,
) : DefaultTask() {
    @get:Input abstract val archivePath: Property<String>
    @get:Input @get:Optional abstract val deviceId: Property<String>

    @TaskAction
    fun run() {
        val archive = File(archivePath.get())
        val app = File(archive, "Products/Applications").listFiles()
            ?.firstOrNull { it.name.endsWith(".app") }
            ?: error("No .app inside ${archive.absolutePath}. Did :iosArchive run with -Pios.teamId set?")

        val resolvedDeviceId = deviceId.orNull ?: run {
            val out = ByteArrayOutputStream()
            execOps.exec {
                commandLine("xcrun", "devicectl", "list", "devices", "--json-output", "-")
                standardOutput = out
            }
            val json = out.toString(Charsets.UTF_8)
            val devicesBlock = Regex("\"devices\"\\s*:\\s*\\[(.*)]\\s*}", RegexOption.DOT_MATCHES_ALL)
                .find(json)?.groupValues?.get(1).orEmpty()
            val candidates = devicesBlock.split("},").mapNotNull { chunk ->
                val ios = chunk.contains("\"platform\" : \"iOS\"") || chunk.contains("\"platform\":\"iOS\"")
                val connected = chunk.contains("\"connected\"")
                val id = Regex("\"identifier\"\\s*:\\s*\"([^\"]+)\"").find(chunk)?.groupValues?.get(1)
                if (ios && connected && id != null) id else null
            }
            candidates.firstOrNull()
                ?: error(
                    "No connected iOS device found. Plug in your iPhone, trust this Mac, and " +
                        "verify with `xcrun devicectl list devices`. Pass -Pios.deviceId=<id> to pin one.",
                )
        }

        logger.lifecycle("Installing ${app.name} to device $resolvedDeviceId …")
        execOps.exec {
            commandLine(
                "xcrun", "devicectl", "device", "install", "app",
                "--device", resolvedDeviceId,
                app.absolutePath,
            )
        }
        logger.lifecycle("Done. Free-Apple-ID signed builds expire after 7 days; re-run to refresh.")
    }
}

tasks.register<InstallOnDeviceTask>("iosInstallDevice") {
    group = "ios"
    description = "Builds, signs, and installs the app onto the first connected iPhone via " +
        "`xcrun devicectl`. Requires -Pios.teamId (free Personal Team id works)."
    dependsOn("iosArchive")
    archivePath.set(iosArchivePath)
    if (project.hasProperty("ios.deviceId")) {
        deviceId.set(project.property("ios.deviceId") as String)
    }
    doFirst {
        val team = iosTeamId ?: discoverTeamIdFromKeychain()
        require(team != null) {
            "iosInstallDevice needs a signing identity but couldn't find one. " +
                "Open Xcode → Settings → Accounts and sign in with your Apple ID once. " +
                "Then re-run; the team id is auto-discovered from the keychain."
        }
    }
}
