import Dependencies.Arrow
import Dependencies.ArrowData
import Dependencies.ArrowTypeClasses
import Dependencies.CommonsIO
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.RxJava2
import Dependencies.SpringDataJpa
import Dependencies.SpringWeb
import Projects.CommonsBio
import Projects.CommonsSerialization
import Projects.CommonsUtil
import Projects.SubmissionSecurity
import SpringBootDependencies.SpringBootAmqp
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(CommonsBio))
    api(project(CommonsUtil))
    api(project(CommonsSerialization))
    api(project(SubmissionSecurity))

    implementation(Arrow)
    implementation(ArrowTypeClasses)
    implementation(ArrowData)
    implementation(CommonsIO)
    implementation(RxJava2)

    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(KotlinLogging)

    implementation(SpringDataJpa)
    implementation(SpringWeb)

    testImplementation(SpringBootAmqp)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
