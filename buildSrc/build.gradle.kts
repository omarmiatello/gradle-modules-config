plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    jcenter()   // JCenter is at end of life
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.0")
    implementation("com.android.tools.build:gradle:7.0.0-alpha15")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.30")
    implementation("org.jetbrains.dokka:dokka-core:1.4.30")
}