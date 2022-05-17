import Dependencies.JSONOrg
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.SpringWeb
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.JsonLibrary
import SpringBootDependencies.SpringRetry
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(CommonsUtil))

    implementation(JSONOrg)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(SpringWeb)
    implementation(KotlinLogging)
    implementation(SpringRetry)

    testApi(project(CommonsTest))
    testApi(project(JsonLibrary))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
