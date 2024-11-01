import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.Log4J
import Projects.ClientBioWebClient
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(ClientBioWebClient))
    implementation(KotlinReflect)
    implementation(KotlinCoroutines)
    implementation(KotlinStdLib)
    implementation(Log4J)
    implementation(KotlinLogging)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
