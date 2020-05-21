import Dependencies.Arrow
import Dependencies.Guava
import Dependencies.JacksonKotlin
import Dependencies.KotlinStdLib
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-util"))
    api(project(":commons:commons-serialization"))

    implementation(Arrow)
    implementation(Guava)
    implementation(KotlinStdLib)
    implementation(JacksonKotlin)

    testApi(project(":commons:commons-test"))

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
