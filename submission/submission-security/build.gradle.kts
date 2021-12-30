import Dependencies.Arrow
import Dependencies.HibernateEntityManager
import Dependencies.Jwt
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.Logback
import Dependencies.RxJava2
import Dependencies.ServletApi
import Dependencies.SpringDataJpa
import Dependencies.SpringSecurityCore
import Dependencies.SpringWeb
import Projects.CommonsBio
import Projects.CommonsHttp
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.EventsPublisher
import Projects.SubmissionPersistenceSql
import SpringBootDependencies.SpringBootAmqp
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.JaxbApi

dependencies {
    api(project(CommonsUtil))
    api(project(CommonsHttp))
    api(project(CommonsBio))
    api(project(CommonsHttp))
    api(project(EventsPublisher))
    api(project(SubmissionPersistenceSql))

    implementation(Arrow)
    implementation(HibernateEntityManager)
    implementation(Jwt)
    implementation(KotlinLogging)
    implementation(JaxbApi)
    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(RxJava2)
    implementation(SpringWeb)
    implementation(SpringSecurityCore)
    implementation(SpringDataJpa)
    implementation(ServletApi)

    testApi(project(CommonsTest))

    testImplementation(Logback)
    testImplementation(SpringBootAmqp)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
