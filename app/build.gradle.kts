import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// API-nycklarna läses från local.properties så att de inte hamnar på GitHub.
val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun secret(name: String): String =
    (localProps.getProperty(name) ?: System.getenv(name) ?: "").trim()

android {
    namespace = "com.resekollen"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.resekollen"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "EXCHANGERATE_KEY", "\"${secret("EXCHANGERATE_KEY")}\"")
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

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Nätverk: Retrofit + Gson för att hämta JSON från API:erna
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
