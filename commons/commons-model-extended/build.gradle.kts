import Dependencies.Arrow
import Dependencies.Guava
import Dependencies.JacksonKotlin
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsTest
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(CommonsUtil))

    implementation(Arrow)
    implementation(Guava)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(JacksonKotlin)

    testApi(project(CommonsTest))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
    testImplementation(testFixtures(project(CommonsModelExtendedSerialization)))
}
