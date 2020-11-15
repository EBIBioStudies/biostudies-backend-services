import Dependencies.HibernateEntityManager
import Dependencies.JpaEntityGraph
import Dependencies.KotlinStdLib
import Dependencies.RxJava2
import Dependencies.SpringDataJpa
import Dependencies.SpringWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":submission:persistence-sql"))

    implementation(HibernateEntityManager)
    implementation(JpaEntityGraph)
    implementation(SpringDataJpa)
    implementation(SpringWeb)
    implementation(KotlinStdLib)
    implementation(RxJava2)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
