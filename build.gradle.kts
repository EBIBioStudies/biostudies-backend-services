import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id(Plugins.KotlinPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.SshPlugin) version PluginVersions.SshVersion
    id(Plugins.JacocoPlugin)
    id(Plugins.DetektPlugin) version PluginVersions.DetektVersion
    id(Plugins.KLintPlugin) version PluginVersions.KtLintVersion
    application
}

apply(from = "deploy.gradle")

allprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = Plugins.KotlinPlugin)
    apply(plugin = Plugins.DetektPlugin)
    apply(plugin = Plugins.KLintPlugin)
    apply(from = "$rootDir/gradle/jacoco.gradle.kts")

    tasks {
        withType<KotlinCompile>().all {
            kotlinOptions {
                jvmTarget = "17"
                freeCompilerArgs =
                    freeCompilerArgs +
                    arrayOf(
//                         "-Xdebug", // uncoment to avoid variables to be optimized out
                        "-Xjvm-default=all",
                        "-opt-in=kotlin.RequiresOptIn",
                        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    )
            }
        }

        withType<Test>().all {
            jvmArgs =
                listOf(
                    "--add-opens",
                    "java.base/java.time=ALL-UNNAMED",
                    "--add-opens",
                    "java.base/java.lang.reflect=ALL-UNNAMED",
                )
        }

        withType<JavaCompile>().all {
            sourceCompatibility = "17"
            targetCompatibility = "17"
        }

        detekt {
            allRules = true
            autoCorrect = true
            buildUponDefaultConfig = true
            config.setFrom("$rootDir/detekt-config.yml")
            source.setFrom("src/main/kotlin")
        }

        withType<Detekt>().configureEach {
            reports {
                html.required.set(true)
            }
        }

        configure<KtlintExtension> {
            ignoreFailures = false
            reporters {
                reporter(ReporterType.HTML)
            }
        }

        check {
            dependsOn(ktlintFormat)
        }
    }
}
