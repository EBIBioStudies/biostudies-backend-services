import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.Jsch
import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsTest
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(CommonsTest))
    api(project(CommonsUtil))

    implementation(Arrow)
    implementation(KotlinCoroutines)
    implementation(KotlinLogging)
    implementation(CommonsIO)
    implementation(Jsch)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
