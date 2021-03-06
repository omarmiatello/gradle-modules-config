version = "1.0.4"

plugins {
    id("java-library")
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    implementation(moduleScript)
    implementation(moduleDependenciesUpdate)
    implementation(Lib.docopt)
    testImplementation(Lib.testJunit)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
