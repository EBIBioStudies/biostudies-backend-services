import Dependencies.Arrow
import Dependencies.Guava
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsBio
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsModelExtendedTest
import Projects.CommonsTest
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(CommonsUtil))
    api(project(CommonsBio))
    api(project(CommonsModelExtendedSerialization))
    api(project(CommonsModelExtendedTest))

    implementation(Arrow)
    implementation(KotlinLogging)
    implementation(Guava)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)

    testApi(project(CommonsTest))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
