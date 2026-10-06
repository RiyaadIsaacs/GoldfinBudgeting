plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
}

import java.io.File
import java.util.Properties

android {
    namespace = "com.example.goldfinbudgeting"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.goldfinbudgeting"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Room local database (POE Part 2)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    //for scrolling
    implementation("androidx.cardview:cardview:1.0.0")

    //for slide out menu
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    //this is the recycleviews for scrolling and when more entries are added or removed
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}

//when the app is installed, point the emulator at Ollama on this computer.
//127.0.0.1 in the emulator is not one person's ip, so the same step works for the whole group.
val forwardOllama = tasks.register<Exec>("forwardOllama") {

    val properties = Properties()

    val localProperties = rootProject.file("local.properties")

    if (localProperties.exists()) {

        localProperties.inputStream().use { properties.load(it) }
    }

    val sdkDir = properties.getProperty("sdk.dir") ?: ""

    val windowsAdb = File(sdkDir, "platform-tools/adb.exe")

    val adb = if (windowsAdb.exists()) windowsAdb else File(sdkDir, "platform-tools/adb")

    commandLine(adb.absolutePath, "reverse", "tcp:11434", "tcp:11434")

    isIgnoreExitValue = true
}

tasks.configureEach {

    if (name == "installDebug" || name == "installRelease") {

        finalizedBy(forwardOllama)
    }
}
