import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Dependencies.Log4J
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":client:bio-webclient"))
    implementation(KotlinStdLib)
    implementation(Log4J)
    implementation(KotlinLogging)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
