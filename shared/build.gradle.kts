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
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "com.happymax.realtimebus.shared"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 3. 如果你需要將高德金鑰注入到 AndroidManifest.xml 中，可以使用 manifestPlaceholders
        val amapKey = envProperties.getProperty("AMAP_KEY") ?: ""
        manifestPlaceholders["AMAP_API_KEY"] = amapKey
        buildConfigField("String", "AMAP_KEY", "\"$amapKey\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // 1. 運行時核心庫 (保持 implementation 不變)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.logging.interceptor)
    implementation(libs.moshi.kotlin)

    // 2. 編譯期代碼生成器 (必須改成 ksp)
    ksp(libs.androidx.room.compiler)       // 負責生成 Room 數據庫的底層 SQL 代碼
    ksp(libs.moshi.kotlin.codegen)         // 負責生成 Moshi 解析 JSON 的轉接器代碼
}