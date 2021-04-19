import com.android.build.gradle.LibraryExtension

/**
 * Credits - Precompiled script plugin from:
 * https://github.com/cortinico/kotlin-android-template/blob/master/buildSrc/src/main/kotlin/publish.gradle.kts
 *
 * The following plugin tasks care of setting up:
 * - Publishing to Maven Central and Sonatype Snapshots
 * - GPG Signing with in memory PGP Keys
 * - Dokka for documentation
 * - sourceJar for attaching sources to publications
 *
 * To use it just apply:
 *
 * plugins {
 *     publish
 * }
 *
 * To your build.gradle.kts.
 *
 * If you copy over this file in your project, make sure to copy it inside: buildSrc/src/main/kotlin/publish.gradle.kts.
 * Make sure to copy over also buildSrc/build.gradle.kts otherwise this plugin will fail to compile due to missing dependencies.
 */
plugins {
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka")
}

val dokkaJar = tasks.create<Jar>("dokkaJar") {
    group = "build"
    description = "Assembles Javadoc jar from Dokka API docs"
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    group = "build"
    description = "Assembles Source jar for publishing"
    archiveClassifier.set("sources")
    if (plugins.hasPlugin("com.android.library")) {
        from((project.extensions.getByName("android") as LibraryExtension).sourceSets.named("main").get().java.srcDirs)
    } else {
        from((project.extensions.getByName("sourceSets") as SourceSetContainer).named("main").get().allSource)
    }
}

tasks.dokkaJavadoc.configure {
    outputDirectory.set(buildDir.resolve("javadoc"))
    dokkaSourceSets {
        configureEach {
            sourceRoot(file("src"))
        }
    }
}


afterEvaluate {
    publishing {
        repositories {
            maven {
                name = "nexus"
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = "NEXUS_USERNAME".byProperty
                    password = "NEXUS_PASSWORD".byProperty
                }
            }
            maven {
                name = "snapshot"
                url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
                credentials {
                    username = "NEXUS_USERNAME".byProperty
                    password = "NEXUS_PASSWORD".byProperty
                }
            }
        }

        publications {
            create<MavenPublication>("release") {
                if (plugins.hasPlugin("com.android.library")) {
                    from(components["release"])
                } else {
                    from(components["java"])
                }
                artifact(dokkaJar)
                artifact(sourcesJar)

                pom {
                    if (!"USE_SNAPSHOT".byProperty.isNullOrBlank()) {
                        version = "$version-SNAPSHOT"
                    }
                    name.set("gradle-modules-config")
                    description.set("Define a configuration and this script will take care of creating the gradle modules")
                    url.set("https://github.com/omarmiatello/gradle-modules-config/")

                    licenses {
                        license {
                            name.set("The MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            id.set("jackl85")
                            name.set("Omar Miatello")
                            email.set("omar.miatello@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/omarmiatello/gradle-modules-config.git")
                        developerConnection.set("scm:git:ssh://github.com/omarmiatello/gradle-modules-config.git")
                        url.set("https://github.com/omarmiatello/gradle-modules-config/")
                    }
                    issueManagement {
                        system.set("GitHub Issues")
                        url.set("https://github.com/omarmiatello/gradle-modules-config/issues")
                    }
                }
            }
        }

        val signingKey = "SIGNING_KEY".byProperty
        val signingPwd = "SIGNING_PWD".byProperty
        if (signingKey.isNullOrBlank() || signingPwd.isNullOrBlank()) {
            logger.info("Signing Disable as the PGP key was not found")
        } else {
            logger.warn("Usign $signingKey - $signingPwd")
            signing {
                useInMemoryPgpKeys(signingKey, signingPwd)
                sign(publishing.publications["release"])
            }
        }
    }
}

val String.byProperty: String? get() = findProperty(this) as? String