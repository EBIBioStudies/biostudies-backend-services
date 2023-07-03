import Dependencies.JacksonCore
import Dependencies.JacksonKotlin
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.ServletApi
import Dependencies.SpringWeb
import Dependencies.SpringWebFlux
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(CommonsUtil))

    implementation(JacksonKotlin)
    implementation(JacksonCore)
    implementation(SpringWeb)
    implementation(SpringWebFlux)
    compileOnly(ServletApi)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
