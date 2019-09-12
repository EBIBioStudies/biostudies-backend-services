import Dependencies.JacksonCore
import Dependencies.JacksonKotlin
import Dependencies.ServletApi
import Dependencies.SpringWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    compile(project(":commons:commons-util"))

    compile(JacksonKotlin)
    compile(JacksonCore)
    compile(SpringWeb)
    compileOnly(ServletApi)

    BaseTestCompileDependencies.forEach { testCompile(it) }
    BaseTestRuntimeDependencies.forEach { testCompile(it) }
}
