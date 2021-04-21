version = "1.0.2"
description = "Dataclasses used to define the configuration for this script"

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
    testImplementation(Lib.testJunit)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
