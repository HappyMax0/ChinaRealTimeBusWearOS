plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "com.happymax.realtimebus.wear"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.happymax.realtimebus"
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
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
    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
}

dependencies {
    // 引入 Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.protolayout)
    implementation(libs.androidx.protolayout.material3)
    implementation(libs.androidx.tiles)
    implementation(libs.androidx.tiles.tooling.preview)
    implementation(libs.androidx.watchface.complications.data.source.ktx)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling)
    implementation(libs.guava)
    implementation(libs.play.services.wearable)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.tiles.renderer)
    debugImplementation(libs.androidx.tiles.tooling)
    implementation(libs.moshi.kotlin)
    "ksp"(libs.androidx.room.compiler)
    "ksp"(libs.moshi.kotlin.codegen)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    // 添加扩展图标库依赖
    implementation("androidx.compose.material:material-icons-extended")
    implementation(project(":shared"))
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
}