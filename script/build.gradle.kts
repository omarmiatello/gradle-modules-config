version = "1.0.1"

plugins {
    id("java-library")
    kotlin("jvm")
    id("maven-publish")
    publish
}

kotlin {
    explicitApi()
}

dependencies {
    api(moduleDataclass)

    testImplementation(Lib.testJunit)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
