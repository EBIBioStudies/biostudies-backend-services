import Dependencies.JSONOrg
import Dependencies.KotlinStdLib
import Dependencies.SpringWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-util"))

    implementation(JSONOrg)
    implementation(KotlinStdLib)
    implementation(SpringWeb)

    testApi(project(":commons:commons-test"))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
