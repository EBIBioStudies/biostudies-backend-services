import Dependencies.Arrow
import Dependencies.JacksonKotlin
import Dependencies.JacksonXml
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.Woodstox

dependencies {
    api(project(CommonsUtil))

    implementation(Arrow)
    implementation(JacksonKotlin)
    implementation(JacksonXml)
    implementation(Woodstox)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
