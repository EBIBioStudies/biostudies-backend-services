import Dependencies.CliKt
import Dependencies.CommonsLang3
import Dependencies.KotlinCoroutines
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.Log4J
import Projects.BioWebClient
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsModelExtendedTest
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

plugins {
    id(Plugins.ShadowPlugin) version PluginVersions.ShadowPluginVersion
}

dependencies {
    api(project(BioWebClient))
    api(project(CommonsModelExtendedTest))
    api(project(CommonsModelExtendedSerialization))
    api(project(CommonsUtil))

    implementation(CliKt)
    implementation(CommonsLang3)
    implementation(KotlinCoroutines)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(Log4J)

    testImplementation(testFixtures(project(CommonsModelExtendedSerialization)))

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}

tasks {
    shadowJar {
        archiveBaseName.set("BioStudiesCLI")
        archiveVersion.set("2.0")
        archiveClassifier.set("")

        manifest {
            attributes(mapOf("Main-Class" to "uk.ac.ebi.biostd.client.cli.BioStudiesCommandLineKt"))
        }
    }
}
