import Dependencies.Guava
import Projects.CommonsTest
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.JsonAssert

dependencies {
    compileOnly(Guava)

    testApi(project(CommonsTest))
    testImplementation(JsonAssert)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
