plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.zapstation"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.zapstation"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation ("com.google.android.exoplayer:exoplayer:2.19.0")
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ui.auth)
    implementation(libs.firebase.storage)
    implementation(libs.preference)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.0")
    implementation("com.github.bumptech.glide:glide:4.13.0")
    implementation(libs.volley)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.maps)
    implementation (libs.fragment)
    implementation (libs.play.services.location)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.ui.firestore)
    implementation (libs.org.eclipse.paho.client.mqttv3)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}