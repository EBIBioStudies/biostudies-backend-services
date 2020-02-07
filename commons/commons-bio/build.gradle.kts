import Dependencies.JacksonDataBind
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    compile(project(":commons:commons-util"))
    // compile(project(":commons:commons-model-extended"))
    compile(JacksonDataBind)

    testCompile(project(":commons:commons-test"))

    BaseTestCompileDependencies.forEach { testCompile(it) }
    BaseTestRuntimeDependencies.forEach { testCompile(it) }
}
