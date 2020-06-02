import Dependencies.Arrow
import Dependencies.Guava
import Dependencies.KotlinStdLib
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-util"))
    api(project(":commons:commons-bio"))

    implementation(Arrow)
    implementation(Guava)
    implementation(KotlinStdLib)

    testApi(project(":commons:commons-test"))

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
