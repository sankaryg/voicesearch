buildscript {

    repositories {
        google()
        jcenter()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.0-alpha07")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.41")
        classpath("com.squareup.sqldelight:gradle-plugin:1.1.3")
        classpath("com.google.gms:google-services:4.3.3")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven("https://jitpack.io")
    }
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}
