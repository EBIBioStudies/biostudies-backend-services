import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.Jsch
import Dependencies.KotlinStdLib
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-util"))

    implementation(Arrow)
    implementation(CommonsIO)
    implementation(Jsch)
    implementation(KotlinStdLib)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
