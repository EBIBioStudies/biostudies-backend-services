import Dependencies.Guava
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.JsonAssert

dependencies {
    compileOnly(Guava)

    testApi(project(":commons:commons-test"))
    testImplementation(JsonAssert)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
