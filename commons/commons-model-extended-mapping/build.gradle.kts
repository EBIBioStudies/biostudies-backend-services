import Dependencies.Arrow
import Dependencies.Guava
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsBio
import Projects.CommonsTest
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(CommonsUtil))
    api(project(CommonsBio))

    implementation(Arrow)
    implementation(Guava)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)

    testApi(project(CommonsTest))

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
