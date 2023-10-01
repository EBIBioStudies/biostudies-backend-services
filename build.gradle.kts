import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(Plugins.DetektPlugin) version PluginVersions.DetektVersion
    id(Plugins.KotlinPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.KtLintPlugin) version PluginVersions.KtLintVersion
    id(Plugins.SshPlugin) version PluginVersions.SshVersion
    id(Plugins.JacocoPlugin)
}

apply(from = "deploy.gradle")

dependencies {
    detektPlugins(Plugins.DetektFormattingPlugin)
}

allprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = Plugins.KotlinPlugin)
    apply(plugin = Plugins.DetektPlugin)
    apply(plugin = Plugins.KtLintPlugin)
    apply(from = "$rootDir/gradle/jacoco.gradle.kts")

    tasks {
        withType<KotlinCompile>().all {
            sourceCompatibility = "11"
            targetCompatibility = "11"
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs =
                    freeCompilerArgs + arrayOf(
                    "-Xjvm-default=enable",
                    "-opt-in=kotlin.RequiresOptIn",
                    "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
                )
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
        "scheduler:tasks:exporter-task:bootJar",
        "scheduler:tasks:pmc-processor-task:bootJar",
        "scheduler:tasks:submission-releaser-task:bootJar",
        "submission:submission-handlers:bootJar",
        "submission:submission-webapp:bootJar"
    )
}
