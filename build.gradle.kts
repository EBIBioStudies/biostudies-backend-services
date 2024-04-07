import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(Plugins.KotlinPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.SshPlugin) version PluginVersions.SshVersion
    id(Plugins.JacocoPlugin)
    application
}

apply(from = "deploy.gradle")

allprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = Plugins.KotlinPlugin)
    apply(from = "$rootDir/gradle/jacoco.gradle.kts")

    tasks {
        withType<KotlinCompile>().all {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs =
                    freeCompilerArgs + arrayOf(
                        "-Xjvm-default=all",
                        "-opt-in=kotlin.RequiresOptIn",
                        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
                    )
            }
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
