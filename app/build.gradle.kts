plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "it.mealmap"
    compileSdk = 37

    defaultConfig {
        applicationId = "it.mealmap"
        minSdk = 26
        targetSdk = 37
        // CI releases number themselves after the workflow run.
        versionCode = System.getenv("GITHUB_RUN_NUMBER")?.toInt() ?: 1
        versionName = "1.0.$versionCode"
    }

    buildTypes {
        release {
            // Signed only when the release keystore is provided (CI); local builds stay unsigned.
            System.getenv("RELEASE_KEYSTORE")?.let { keystore ->
                signingConfig =
                    signingConfigs.create("release") {
                        storeFile = file(keystore)
                        storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                        keyAlias = "meal-map"
                        keyPassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                    }
            }
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                // Robolectric on API 36 needs access to JDK internals.
                it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
            }
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.kotlinx.serialization.json)

    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
}
