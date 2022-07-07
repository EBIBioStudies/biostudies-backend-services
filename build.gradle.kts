import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("org.hidetake.ssh") version "2.10.1"
    id("jacoco")
}

apply(from = "deploy.gradle")

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")
}

allprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(from = "$rootDir/gradle/jacoco.gradle.kts")

    tasks {
        withType<KotlinCompile>().all {
            sourceCompatibility = "11"
            targetCompatibility = "11"
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs = freeCompilerArgs + arrayOf("-Xjvm-default=enable", "-opt-in=kotlin.RequiresOptIn")
            }
        }

        detekt {
            allRules = true
            autoCorrect = true
            buildUponDefaultConfig = true
            config = files("$rootDir/detekt-config.yml")
            source = files("src/main/kotlin")
        }

        withType<Detekt> {
            reports {
                html.required.set(true)
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
        "submission:submission-webapp:bootJar",
        "scheduler:scheduler:bootJar",
        "scheduler:tasks:pmc-processor-task:bootJar",
        "scheduler:tasks:submission-releaser-task:bootJar",
        "submission:submission-handlers:bootJar",
        "submission:submission-webapp:bootJar"
    )
}
