import dev.detekt.gradle.Detekt
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("org.jetbrains.kotlinx.kover")
    id("dev.detekt")
}

val libs = the<LibrariesForLibs>()

detekt {
    toolVersion = libs.versions.detekt.get()

    source.from(
        files("src/main/kotlin"),
        files("src/test/kotlin"),
        files("src/commonMain/kotlin"),
        files("src/commonTest/kotlin"),
        files("src/commonTestFixtures/kotlin"),
        files("src/jvmMain/kotlin"),
        files("src/jvmTest/kotlin"),
        files("src/androidMain/kotlin"),
        files("src/androidTest/kotlin"),
        files("src/iosMain/kotlin"),
        files("src/iosTest/kotlin"),
        files("src/iosX64Main/kotlin"),
        files("src/iosX64Test/kotlin"),
        files("src/iosArm64Main/kotlin"),
        files("src/iosArm64Test/kotlin"),
        files("src/iosSimulatorArm64Main/kotlin"),
        files("src/iosSimulatorArm64Test/kotlin"),
        files("src/desktopMain/kotlin"),
        files("src/desktopTest/kotlin"),
        files("src/testFixtures/kotlin")
    )

    parallel = true

    config.from(rootProject.file("detekt.yml"))
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required = true
        html.outputLocation = file("build/reports/detekt.html")
    }
}

kover {
    reports {
        verify.rule {
            minBound(
                minValue = 85,
                coverageUnits = CoverageUnit.LINE,
                aggregationForGroup = AggregationType.COVERED_PERCENTAGE,
            )
        }
    }
}
