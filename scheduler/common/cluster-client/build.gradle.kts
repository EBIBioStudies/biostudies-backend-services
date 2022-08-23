import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.Jsch
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(CommonsUtil))

    implementation(Arrow)
    implementation(KotlinLogging)
    implementation(CommonsIO)
    implementation(Jsch)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
