import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.io.FileInputStream
import java.util.Properties

// 1. 讀取根目錄下的 .env 檔案
val envFile = project.rootProject.file(".env.example")
val envProperties = Properties()
if (envFile.exists()) {
    envProperties.load(FileInputStream(envFile))
} else {
    // 找不到 .env 時的備用處理，可以選擇報錯或給預設值
    println("警告: 找不到 .env 檔案，請根據 .env.example 創建一份")
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.secrets)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.happymax.realtimebus"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.happymax.realtimebus"
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 3. 如果你需要將高德金鑰注入到 AndroidManifest.xml 中，可以使用 manifestPlaceholders
        val amapKey = envProperties.getProperty("AMAP_KEY") ?: ""
        manifestPlaceholders["AMAP_API_KEY"] = amapKey
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"
    ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

    googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))
    // implementation(libs.accompanist.permissions)
    implementation(libs.androidx.activity.compose)
    // implementation(libs.androidx.camera.camera2)
    // implementation(libs.androidx.camera.core)
    // implementation(libs.androidx.camera.lifecycle)
    // implementation(libs.androidx.camera.view)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    // implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    // implementation(libs.coil.compose)
    implementation(libs.converter.moshi)
    implementation(libs.firebase.ai)
    // Uncomment to use Firestore:
    // implementation(libs.firebase.firestore)

    // Uncomment ALL FOUR of the following dependencies together to use Firebase Auth and Google
    // Sign-In via Credential Manager:
    // implementation(libs.firebase.auth)
    // implementation(libs.androidx.credentials)
    // implementation(libs.androidx.credentials.play.services)
    // implementation(libs.googleid)
    implementation(libs.firebase.appcheck.recaptcha)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logging.interceptor)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    // implementation(libs.play.services.location)
    implementation(libs.retrofit)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    "ksp"(libs.androidx.room.compiler)
    "ksp"(libs.moshi.kotlin.codegen)
    implementation(project(":shared"))
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
}
