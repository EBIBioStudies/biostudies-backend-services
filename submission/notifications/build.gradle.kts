import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.HibernateEntityManager
import Dependencies.JpaEntityGraph
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.RxJava2
import Dependencies.SpringDataJpa
import Dependencies.SpringWeb
import Projects.CommonsModelExtended
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.SubmissionConfig
import Projects.SubmissionPersistenceCommonApi
import Projects.SubmissionPersistenceSql
import SpringBootDependencies.SpringBootStarterMail
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(CommonsModelExtended))
    api(project(SubmissionConfig))
    api(project(SubmissionPersistenceCommonApi))
    api(project(SubmissionPersistenceSql))

    api(project(CommonsTest))
    api(project(CommonsUtil))

    implementation("$SpringBootStarterMail:${Versions.SpringBootVersion}")
    implementation(Arrow)
    implementation(CommonsIO)
    implementation(HibernateEntityManager)
    implementation(JpaEntityGraph)
    implementation(SpringDataJpa)
    implementation(SpringWeb)
    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(RxJava2)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
