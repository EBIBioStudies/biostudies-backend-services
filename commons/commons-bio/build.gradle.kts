import Dependencies.Arrow
import Dependencies.JacksonDataBind
import Dependencies.JavaValidationApi
import Dependencies.KotlinCoroutines
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsModelExtended
import Projects.CommonsTest
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.KotlinCoroutinesTest

dependencies {
    api(project(CommonsUtil))
    api(project(CommonsModelExtended))

    implementation(Arrow)
    implementation(JacksonDataBind)
    implementation(JavaValidationApi)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(KotlinCoroutines)

    testApi(project(CommonsTest))
    testImplementation(KotlinCoroutinesTest)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
