import Dependencies.JSONOrg
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.SpringWeb
import Projects.CommonsTest
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(CommonsUtil))

    implementation(JSONOrg)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(SpringWeb)

    testApi(project(CommonsTest))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
