import Dependencies.JSONOrg
import Dependencies.KotlinStdLib
import Dependencies.SpringWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-util"))
    api(project(":commons:commons-http"))
    api(project(":commons:commons-bio"))
    api(project(":commons:commons-serialization"))
    api(project(":commons:commons-model-extended-serialization"))

    implementation(JSONOrg)
    implementation(KotlinStdLib)
    implementation(SpringWeb)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
