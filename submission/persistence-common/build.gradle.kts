import Dependencies.Arrow
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-util"))
    api(project(":commons:commons-model-extended"))
    api(project(":commons:commons-serialization"))
    api(project(":commons:commons-bio"))
    api(project(":commons:commons-model-extended-mapping"))

    implementation(Arrow)
    implementation(KotlinStdLib)
    implementation(KotlinLogging)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
