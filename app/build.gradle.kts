import com.abhi.buildsrc.Libs
import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

androidExtensions {
    isExperimental = true
}

android {
    signingConfigs {
        register("release") {

            val keystorePropertiesFile = file("../upload-keystore.properties")

            if (!keystorePropertiesFile.exists()) {
                logger.warn("Release builds may not work: signing config not found.")
                return@register
            }

            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))

            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "com.abhi.voicesearch"
        minSdkVersion(21)
        targetSdkVersion(28)
        versionCode = 10
        versionName = "0.9"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        named("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
            signingConfig = signingConfigs.getByName("release")
        }
//        named("debug") {
//            applicationIdSuffix = ".debug"
//        }
    }
    kapt.correctErrorTypes = true
    lintOptions.isAbortOnError = false
    
    buildFeatures {
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(project(":base"))
    implementation(project(":base-android"))

    // Kotlin
    implementation(Libs.Kotlin.stdlib)
    implementation(Libs.Coroutines.core)
    implementation(Libs.Coroutines.android)

    //fcm
    implementation("com.google.firebase:firebase-core:17.0.0")
    implementation("com.google.firebase:firebase-iid:19.0.1")
    implementation("com.google.firebase:firebase-messaging:19.0.1")
    // Google
    implementation(Libs.Google.material)
    implementation(Libs.AndroidX.coreKtx)
    implementation(Libs.AndroidX.constraintlayout)
    implementation(Libs.AndroidX.appcompat)
    implementation(Libs.AndroidX.recyclerview)
    implementation(Libs.AndroidX.Fragment.fragmentKtx)
    implementation(Libs.AndroidX.fastscroller)

    implementation("androidx.palette:palette:1.0.0")

    // Navigation
    implementation(Libs.AndroidX.Navigation.navigationUi)
    implementation(Libs.AndroidX.Navigation.navigationFragment)
    implementation("com.google.code.gson:gson:2.8.6")

    // Room
    kapt(Libs.AndroidX.Room.compiler)
    implementation(Libs.AndroidX.Room.runtime)
    implementation(Libs.AndroidX.Room.roomktx)
    implementation(Libs.AndroidX.Room.rxjava2)

    // LiveData
    implementation(Libs.AndroidX.Lifecycle.extensions)

    // Paging
    implementation(Libs.AndroidX.Paging.runtimeKtx)

    // Work
    implementation(Libs.AndroidX.Work.runtimeKtx)

    // Dagger
    implementation(Libs.Dagger.dagger)
    kapt(Libs.Dagger.compiler)

    implementation(Libs.Dagger.androidSupport)
    kapt(Libs.Dagger.androidProcessor)

    compileOnly(Libs.AssistedInject.annotationDagger2)
    kapt(Libs.AssistedInject.processorDagger2)

    // Epoxy
    implementation(Libs.Epoxy.epoxy)
    implementation(Libs.Epoxy.dataBinding)
    implementation(Libs.Epoxy.paging)
    kapt(Libs.Epoxy.processor)

    implementation(Libs.MvRx.main)
    testImplementation(Libs.MvRx.testing)

    // RxJava
    implementation(Libs.RxJava.rxJava)
    implementation(Libs.RxJava.rxAndroid)
    implementation(Libs.RxJava.rxKotlin)
    implementation(Libs.RxJava.rxRelay)
    implementation(Libs.RxJava.rxkPrefs)

    implementation(Libs.materialDialogs)
    implementation(Libs.materialDialogBottomSheet)
    implementation(Libs.stetho)
    implementation(Libs.logger)

    implementation(Libs.notify)

    debugImplementation(Libs.LeakCanary.no_op)
//    debugImplementation(Libs.LeakCanary.support)
    releaseImplementation(Libs.LeakCanary.no_op)

    // Iconics
    implementation("com.mikepenz:iconics-core:3.1.0@aar")
    implementation("com.mikepenz:community-material-typeface:2.0.46.1@aar")
    implementation("com.mikepenz:google-material-typeface:3.0.1.2.original@aar")

    // UI
    implementation("com.reddit:indicator-fast-scroll:1.0.1")

    // Time
    implementation(Libs.timeAgo)

    // Debugging
    implementation(Libs.junit)
    testImplementation("org.mockito:mockito-core:2.24.5")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
}
