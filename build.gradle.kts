import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    id("org.jetbrains.kotlin.jvm") version "1.6.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    id("org.hidetake.ssh") version "2.10.1"
    id("jacoco")
}

apply(from = "deploy.gradle")

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")
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
        withType<KotlinCompile>().all {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = freeCompilerArgs + arrayOf("-Xjvm-default=enable")
            }
        }

        withType<Detekt>().configureEach {
            jvmTarget = "1.8"
            reports {
                html {
                    required.set(true)
                    outputLocation.set(file("build/reports/detekt.html"))
                }
            }
        }

        detekt {
            allRules = true
            autoCorrect = true
            buildUponDefaultConfig = true
            config = files("$rootDir/detekt-config.yml")
            source = files("src/main/kotlin")
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
        "scheduler:exporter-task:bootJar",
        "scheduler:scheduler:bootJar",
        "scheduler:pmc-processor-task:bootJar",
        "scheduler:submission-releaser-task:bootJar",
        "submission:submission-webapp:bootJar"
    )
}
