plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kVision)
}

version = "1.0.1"
group = "cz.rblaha15.fotbaly_ve_ctvrtek"

kotlin {
    js(IR) {
        moduleName = "jsApp"
        browser {
            commonWebpackConfig {
                outputFileName = "main.bundle.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            dependencies {
                implementation(libs.kvision)
                implementation(libs.kvision.bootstrap)
                implementation(libs.kvision.state)
                implementation(libs.kvision.state.flow)
                implementation(libs.kvision.routing.navigo.ng)
                implementation(projects.shared)
                implementation(libs.lifecycle.viewmodel.compose)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}