import Dependencies.CliKt
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
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
    api(project(":commons:commons-serialization"))

    implementation(CliKt)
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
        archiveBaseName.set("FilesTableGenerator")
        manifest {
            attributes(mapOf("Main-Class" to "ac.uk.ebi.transpiler.FilesTableGeneratorKt"))
        }
    }
}
