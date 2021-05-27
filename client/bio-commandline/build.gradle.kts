import Dependencies.CliKt
import Dependencies.CommonsLang3
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.Log4J
import Dependencies.SpringWeb
import TestDependencies.AssertJ
import TestDependencies.Junit
import TestDependencies.Junit5Console
import TestDependencies.JunitExtensions
import TestDependencies.KotlinTestJunit
import TestDependencies.MockK

plugins {
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

dependencies {
    api(project(":commons:commons-util"))
    api(project(":client:bio-webclient"))

    implementation(CliKt)
    implementation(Log4J)
    implementation(SpringWeb)
    implementation(CommonsLang3)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)

    testImplementation(Junit)
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
