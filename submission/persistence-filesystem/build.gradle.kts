import Dependencies.Arrow
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsBio
import Projects.CommonsModelExtended
import Projects.CommonsModelExtendedMapping
import Projects.CommonsSerialization
import Projects.CommonsUtil
import Projects.SubmissionPersistenceCommonApi
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(CommonsBio))
    api(project(CommonsSerialization))
    api(project(CommonsModelExtended))
    api(project(CommonsModelExtendedMapping))
    api(project(CommonsUtil))
    api(project(SubmissionPersistenceCommonApi))

    implementation(Arrow)
    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(KotlinLogging)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
