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
    compile(CliKt)
    compile(project(":commons:commons-util"))
    compile(project(":commons:commons-serialization"))

    testCompile(Junit)
    testCompile(JunitExtensions)
    testCompile(AssertJ)
    testCompile(MockK)
    testCompile(KotlinTestJunit)
    testRuntime(Junit5Console)
}

tasks {
    shadowJar {
        archiveBaseName.set("FilesTableGenerator")
        manifest {
            attributes(mapOf("Main-Class" to "ac.uk.ebi.transpiler.FilesTableGeneratorKt"))
        }
    }
}
