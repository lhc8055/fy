import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    android {
        minSdk = 23
        compileSdk = 36
        buildToolsVersion = "36.1.0"
        namespace = "com.kyant.backdrop.catalog.common"
        androidResources.enable = true
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain = getByName("commonMain") {
            dependencies {
                implementation(libs.compose.foundation)
                implementation(libs.compose.ui)
                implementation(libs.compose.ui.graphics)
                implementation(libs.compose.resources)
                implementation(libs.compose.material.ripple)
                implementation(libs.kyant.shapes)
                implementation(project(":backdrop"))
            }
        }

        val androidMain = getByName("androidMain") {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xlambdas=class"
        )
    }
}
