plugins {
    id("io.gitlab.arturbosch.detekt") version "1.0.1"
    id("org.jetbrains.kotlin.jvm") version "1.3.50"
    id("org.jlleitschuh.gradle.ktlint") version "8.2.0"
    id("org.hidetake.ssh") version "2.10.1"
    id("jacoco")
}

apply(from = "deploy.gradle")

allprojects {
    repositories {
        jcenter()
        mavenLocal()
        mavenCentral()
        maven(url = "https://oss.sonatypeorg/content/repositories/snapshots")
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    apply(from = "$rootDir/gradle/jacoco.gradle.kts")

    tasks {
        detekt {
            version = "1.0.1"
            config = files("$rootDir/detekt-config.yml")
            input = files("src/main/kotlin")
            filters = ".*/resources/.*,.*/build/.*"
            reports {
                html {
                    enabled = true
                    destination = file("build/reports/detekt.html")
                }
            }
        }

        compileKotlin {
            sourceCompatibility = "1.8"
            targetCompatibility = "1.8"

            kotlinOptions {
                includeRuntime = true
                jvmTarget = "1.8"
            }
        }

        compileTestKotlin {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }

        check {
            dependsOn(ktlintFormat)
        }
    }
}
