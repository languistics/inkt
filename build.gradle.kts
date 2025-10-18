import java.time.LocalDateTime

plugins {
    idea
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    id("maven-publish")
    signing
}

/**
 * Provides a semi-readable qualifier for local publications
 */
fun getVersionTimestamp(): String = with(LocalDateTime.now()) {
    year.toString() +
            monthValue.toString().padStart(2, '0') +
            dayOfMonth.toString().padStart(2, '0') +
            hour.toString().padStart(2, '0') +
            minute.toString().padStart(2, '0') +
            second.toString().padStart(2, '0')
}

allprojects {
    if (version.toString().isBlank() || version.toString() == "unspecified") {
        // If the version hasn't been specified, set it to a timestamped default
        version = "LOCAL-${getVersionTimestamp()}"
    } else if (version.toString().startsWith("v")) {
        // TODO: Probably should do this before passing as a parameter
        version = version.toString().drop(1)
    }
    if (project == rootProject) println("Using version for build: $version")

    repositories {
        mavenCentral()
    }

    // configure Kotlin to allow these opt-in features throughout the project
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.time.ExperimentalTime",
                "-opt-in=kotlin.contracts.ExperimentalContracts",
                "-Xcontext-receivers"
            )
        }
    }
}

dependencies {
    testImplementation(libs.kotlin.test)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit.jupiter)
    testRuntimeOnly(libs.bundles.junit.runtime)
}

java {
    withJavadocJar()
    withSourcesJar()
}

val javaLanguageVersion: String by project
kotlin {
    explicitApiWarning()
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(javaLanguageVersion))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kover {
    reports {
        verify {
            rule {
                minBound(90)
            }
        }

        total {
            html {
                onCheck = true
            }
        }
    }
}

publishing {
    repositories {
        maven {
            name = "OSSRH"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
        maven {
            name = "GitHubPackages"
            setUrl("https://maven.pkg.github.com/ty1824/dialector")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("default") {
            from(components["java"])
            pom {
                name.set("inkt")
                description.set("Incremental computation framework for Kotlin")
                url.set("http://dialector.dev")
                licenses {
                    license {
                        name.set("GPL-3.0")
                        url.set("https://opensource.org/licenses/GPL-3.0")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/ty1824/dialector/issues")
                }
                scm {
                    connection.set("https://github.com/ty1824/dialector.git")
                    url.set("https://github.com/ty1824/dialector")
                }
                developers {
                    developer {
                        name.set("Tyler Hodgkins")
                        email.set("ty1824@gmail.com")
                    }
                }
            }
        }
    }
}

signing {
    val gpgPrivateKey = System.getenv("GPG_SIGNING_KEY")
    if (!gpgPrivateKey.isNullOrBlank()) {
        useInMemoryPgpKeys(
            gpgPrivateKey,
            System.getenv("GPG_SIGNING_PASSPHRASE")
        )
        sign(publishing.publications)
    }
}
