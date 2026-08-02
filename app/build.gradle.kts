import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.myapp"
    compileSdk = 37
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        // ===== ТОЛЬКО arm64-v8a =====
        // Один APK, в котором есть нативные библиотеки исключительно для arm64-v8a.
        // ВАЖНО: нельзя одновременно задавать ndk.abiFilters и splits.abi —
        // AGP падает с "Conflicting configuration ... in ndk abiFilters
        // cannot be present when splits abi filters are set".
        // splits.abi нужен только для НЕСКОЛЬКИХ APK (по одному на ABI).
        // Нам нужен один — поэтому используем abiFilters.
        ndk {
            abiFilters.clear()
            abiFilters += "arm64-v8a"
        }
    }

    // ===== Подпись релиза =====
    // Ключ и пароли берутся из keystore.properties (создать: rai keystore create).
    // Файл в .gitignore — секреты не попадают в репозиторий.
    signingConfigs {
        create("release") {
            val propsFile = rootProject.file("keystore.properties")
            if (propsFile.exists()) {
                val props = Properties()
                propsFile.inputStream().use { props.load(it) }
                storeFile = file(props.getProperty("storeFile"))
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
                // современные схемы подписи
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = false
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            // подписываем, только если ключ реально настроен
            signingConfig = if (rootProject.file("keystore.properties").exists())
                signingConfigs.getByName("release") else null

            isMinifyEnabled = true          // R8: сжатие и обфускация кода
            isShrinkResources = true        // выбросить неиспользуемые ресурсы
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    // современный DSL вместо устаревшего kotlinOptions { jvmTarget }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // на всякий случай выкидываем чужие ABI из зависимостей
            excludes += listOf(
                "**/x86/**", "**/x86_64/**", "**/armeabi-v7a/**", "**/armeabi/**"
            )
        }
    }
    lint {
        abortOnError = false        // на телефоне lint часто мешает
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.0")

    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
