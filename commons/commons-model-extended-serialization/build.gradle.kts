import Dependencies.Arrow
import Dependencies.JacksonKotlin
import Dependencies.JacksonXml
import Dependencies.KotlinStdLib
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-model-extended"))

    implementation(Arrow)
    implementation(JacksonKotlin)
    implementation(JacksonXml)
    implementation(KotlinStdLib)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
