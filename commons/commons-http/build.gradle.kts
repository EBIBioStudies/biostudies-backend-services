import Dependencies.JacksonCore
import Dependencies.JacksonKotlin
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.ServletApi
import Dependencies.SpringWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-util"))

    implementation(JacksonKotlin)
    implementation(JacksonCore)
    implementation(SpringWeb)
    compileOnly(ServletApi)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
