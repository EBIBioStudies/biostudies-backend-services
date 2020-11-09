import Dependencies.Arrow
import Dependencies.HibernateEntityManager
import Dependencies.Jwt
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Dependencies.Logback
import Dependencies.RxJava2
import Dependencies.ServletApi
import Dependencies.SpringDataJpa
import Dependencies.SpringSecurityCore
import Dependencies.SpringWeb
import SpringBootDependencies.SpringBootAmqp
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-util"))
    api(project(":commons:commons-http"))
    api(project(":commons:commons-bio"))
    api(project(":commons:commons-http"))
    api(project(":events:events-publisher"))
    api(project(":submission:persistence"))

    implementation(Arrow)
    implementation(HibernateEntityManager)
    implementation(Jwt)
    implementation(KotlinLogging)
    implementation(KotlinStdLib)
    implementation(RxJava2)
    implementation(SpringWeb)
    implementation(SpringSecurityCore)
    implementation(SpringDataJpa)
    implementation(ServletApi)

    testApi(project(":commons:commons-test"))

    testImplementation(Logback)
    testImplementation(SpringBootAmqp)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
