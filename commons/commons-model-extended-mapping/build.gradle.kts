import Dependencies.Guava
import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsBio
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsModelExtendedTest
import Projects.CommonsSerialization
import Projects.CommonsTest
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.KotlinCoroutinesTest

dependencies {
    api(project(CommonsUtil))
    api(project(CommonsBio))
    api(project(CommonsModelExtendedSerialization))
    api(project(CommonsModelExtendedTest))
    api(project(CommonsSerialization))

    implementation(KotlinLogging)
    implementation(Guava)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(KotlinCoroutines)

    testApi(project(CommonsTest))
    testImplementation(KotlinCoroutinesTest)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
