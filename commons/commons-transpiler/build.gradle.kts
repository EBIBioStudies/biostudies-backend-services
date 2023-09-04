import Dependencies.CliKt
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsSerialization
import Projects.CommonsUtil
import TestDependencies.AssertJ
import TestDependencies.Junit
import TestDependencies.Junit5Console
import TestDependencies.JunitExtensions
import TestDependencies.KotlinTestJunit
import TestDependencies.MockK

plugins {
    id(Plugins.ShadowPlugin) version PluginVersions.ShadowPluginVersion
}

dependencies {
    api(project(CommonsUtil))
    api(project(CommonsSerialization))

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
