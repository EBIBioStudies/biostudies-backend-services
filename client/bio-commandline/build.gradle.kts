import Dependencies.CliKt
import Dependencies.CommonsLang3
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.Log4J
import Projects.BioWebClient
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsModelExtendedTest
import Projects.CommonsUtil
import TestDependencies.AssertJ
import TestDependencies.Junit
import TestDependencies.Junit5Console
import TestDependencies.JunitExtensions
import TestDependencies.KotlinCoroutinesTest
import TestDependencies.KotlinTestJunit
import TestDependencies.MockK

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
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(Log4J)

    testImplementation(testFixtures(project(CommonsModelExtendedSerialization)))
    testImplementation(Junit)
    testImplementation(KotlinCoroutinesTest)
    testImplementation(JunitExtensions)
    testImplementation(AssertJ)
    testImplementation(MockK)
    testImplementation(KotlinTestJunit)
    testRuntimeOnly(Junit5Console)
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
