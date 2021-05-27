import Dependencies.JSONOrg
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.SpringWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-util"))

    implementation(JSONOrg)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(SpringWeb)

    testApi(project(":commons:commons-test"))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
