import Dependencies.Arrow
import Dependencies.JacksonKotlin
import Dependencies.JacksonXml
import Dependencies.KotlinStdLib
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-model-extended"))
    api(project(":commons:commons-serialization-util"))

    implementation(Arrow)
    implementation(JacksonKotlin)
    implementation(JacksonXml)
    implementation(KotlinStdLib)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
