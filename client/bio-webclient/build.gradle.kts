import Dependencies.JSONOrg
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.ReactorNetty
import Dependencies.SpringWebFlux
import Projects.CommonsBio
import Projects.CommonsHttp
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsSerialization
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(CommonsUtil))
    api(project(CommonsHttp))
    api(project(CommonsBio))
    api(project(CommonsSerialization))
    api(project(CommonsModelExtendedSerialization))

    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(JSONOrg)
    implementation(KotlinStdLib)
    implementation(ReactorNetty)
    implementation(SpringWebFlux)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
