import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsBio
import Projects.CommonsModelExtended
import SpringBootDependencies.SpringBootAmqp
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(CommonsModelExtended))
    api(project(CommonsBio))

    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(SpringBootAmqp)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
