plugins {
    id("io.gitlab.arturbosch.detekt") version "1.16.0"
    id("org.jetbrains.kotlin.jvm") version "1.4.32"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    id("org.hidetake.ssh") version "2.10.1"
    id("jacoco")
}

apply(from = "deploy.gradle")

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.16.0")
}

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
            allRules = true
            autoCorrect = true
            buildUponDefaultConfig = true
            config = files("$rootDir/detekt-config.yml")
            input = files("src/main/kotlin")
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
                freeCompilerArgs = freeCompilerArgs + arrayOf("-Xjvm-default=enable")
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

tasks.register("buildArtifacts") {
    dependsOn(
        "client:bio-commandline:shadowJar",
        "bio-admin:bootJar",
        "scheduler:scheduler:bootJar",
        "scheduler:pmc-processor-task:bootJar",
        "scheduler:submission-releaser-task:bootJar",
        "submission:submission-webapp:bootJar"
    )
}
