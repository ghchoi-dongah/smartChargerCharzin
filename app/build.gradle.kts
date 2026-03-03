plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.dongah.smartcharger"
    compileSdk = 35

    signingConfigs {
        getByName("debug") {
            keyAlias = "systemkey"
            keyPassword = "123456"
            storeFile = file("D:\\AndroidDongah\\PlatformKeyClear\\platform.jks")
            storePassword = "123456"
        }
    }

    defaultConfig {
        applicationId = "com.dongah.smartcharger"
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
    externalNativeBuild {
        ndkBuild {
            path = file("src/main/jni/Android.mk")
        }
    }
    ndkVersion = "26.1.10909125"
}

dependencies {
    implementation(libs.rxandroid)
    implementation(libs.rxjava)
    implementation(libs.okhttp)
    implementation(libs.okhttptls)
    implementation(libs.zxing)
    implementation(libs.jsch)
    implementation(libs.slf4j)
    implementation(libs.gson)
    implementation(libs.jaxb)
    implementation(libs.backport)
    implementation(libs.circulrprogress)
    implementation(libs.bouncycastle)
    implementation(libs.cardview)
    implementation(libs.commonsnet)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}