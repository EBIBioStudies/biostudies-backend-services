import Dependencies.CliKt
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
    compile(project(":commons:commons-util"))
    compile(project(":client:bio-webclient"))

    compile(CliKt)

    testCompile(Junit)
    testCompile(JunitExtensions)
    testCompile(AssertJ)
    testCompile(MockK)
    testCompile(KotlinTestJunit)
    testRuntime(Junit5Console)
}

tasks {
    shadowJar {
        archiveBaseName.set("PTSubmit")
        archiveVersion.set("2.0")
        archiveClassifier.set("")

        manifest {
            attributes(mapOf("Main-Class" to "uk.ac.ebi.biostd.client.cli.BioStudiesCommandLineKt"))
        }
    }
}
