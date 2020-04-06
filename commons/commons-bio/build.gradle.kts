import Dependencies.Arrow
import Dependencies.JacksonDataBind
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-util"))

    implementation(kotlin("stdlib"))
    implementation(Arrow)
    implementation(JacksonDataBind)

    testApi(project(":commons:commons-test"))

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
