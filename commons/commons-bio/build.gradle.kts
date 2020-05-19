import Dependencies.Arrow
import Dependencies.JacksonDataBind
import Dependencies.JavaValidationApi
import Dependencies.KotlinStdLib
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-util"))
    api(project(":commons:commons-model-extended"))

    implementation(Arrow)
    implementation(JacksonDataBind)
    implementation(JavaValidationApi)
    implementation(KotlinStdLib)

    testApi(project(":commons:commons-test"))

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
