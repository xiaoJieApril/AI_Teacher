import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.lolha.learningapp"
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    defaultConfig {
        applicationId = "com.lolha.learningapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        val localProperties = Properties().apply {
            val file = rootProject.file("local.properties")
            if (file.exists()) {
                file.inputStream().use { input ->
                    this.load(input)
                }
            }
        }
        val geminiApiKey = providers.gradleProperty("GEMINI_API_KEY").orNull
            ?: localProperties.getProperty("GEMINI_API_KEY").orEmpty()
        val supabaseUrl = providers.gradleProperty("SUPABASE_URL").orNull
            ?: localProperties.getProperty("SUPABASE_URL").orEmpty()
        val supabaseAnonKey = providers.gradleProperty("SUPABASE_ANON_KEY").orNull
            ?: localProperties.getProperty("SUPABASE_ANON_KEY").orEmpty()
        val supabaseAuthEmail = providers.gradleProperty("SUPABASE_AUTH_EMAIL").orNull
            ?: localProperties.getProperty("SUPABASE_AUTH_EMAIL").orEmpty()
        val supabaseAuthPassword = providers.gradleProperty("SUPABASE_AUTH_PASSWORD").orNull
            ?: localProperties.getProperty("SUPABASE_AUTH_PASSWORD").orEmpty()
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField("String", "SUPABASE_AUTH_EMAIL", "\"$supabaseAuthEmail\"")
        buildConfigField("String", "SUPABASE_AUTH_PASSWORD", "\"$supabaseAuthPassword\"")
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    val roomVersion = "2.6.1"

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material:material-icons-extended:1.7.6")
    implementation("androidx.compose.ui:ui:1.7.6")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.6")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    ksp("androidx.room:room-compiler:$roomVersion")

    debugImplementation("androidx.compose.ui:ui-tooling:1.7.6")
}
