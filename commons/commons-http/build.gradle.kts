import Dependencies.JacksonCore
import Dependencies.JacksonKotlin
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

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
