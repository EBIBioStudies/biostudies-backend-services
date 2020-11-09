import Dependencies.KotlinStdLib
import SpringBootDependencies.SpringBootAmqp
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-model-extended"))
    api(project(":commons:commons-bio"))

    implementation(KotlinStdLib)
    implementation(SpringBootAmqp)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
