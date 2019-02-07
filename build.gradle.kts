plugins {
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC12"
    id("org.jetbrains.kotlin.jvm") version "1.3.20"
    id("org.jlleitschuh.gradle.ktlint") version "7.1.0"
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        mavenLocal()
        maven(url = "https://oss.sonatypeorg/content/repositories/snapshots")
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    tasks {

        detekt {
            version = "1.0.0.RC7-3"
            config = files("$rootDir/deteck-config.yml")
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

        test {
            useJUnitPlatform()
        }

        build {
            dependsOn(ktlintFormat)
        }
    }
}
