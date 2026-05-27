plugins {
    id("kmp-web-library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

android {
    namespace = "com.y9vad9.minesweeper.data"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    sourceSets {
        val sqlMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                api(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines.extensions)
            }
        }
        jvmMain {
            dependsOn(sqlMain)
            dependencies {
                implementation(libs.sqldelight.driver.jvm)
            }
        }
        androidMain {
            dependsOn(sqlMain)
            dependencies {
                implementation(libs.sqldelight.driver.android)
            }
        }
        iosMain {
            dependsOn(sqlMain)
            dependencies {
                implementation(libs.sqldelight.driver.native)
            }
        }
        commonMain.dependencies {
            implementation(projects.logic)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmTest.dependencies {
            implementation(libs.sqldelight.driver.jvm)
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.sqldelight.driver.android)
                implementation(libs.robolectric)
                implementation(libs.androidx.test.core)
            }
        }
        iosTest.dependencies {
            implementation(libs.sqldelight.driver.native)
        }
        wasmJsTest.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}

sqldelight {
    databases {
        create("MinesweeperDatabase") {
            packageName.set("com.y9vad9.minesweeper.data.db")
            srcDirs.setFrom("src/sqlMain/sqldelight")
        }
    }
}
