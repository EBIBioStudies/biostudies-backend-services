import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.HibernateEntityManager
import Dependencies.JpaEntityGraph
import Dependencies.KotlinStdLib
import Dependencies.SpringDataJpa
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

plugins {
    id("org.jetbrains.kotlin.plugin.jpa") version "1.3.10"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.10"
}

dependencies {
    api(project(":commons:commons-util"))
    api(project(":commons:commons-bio"))
    api(project(":commons:commons-serialization"))
    api(project(":commons:commons-model-extended"))
    api(project(":commons:commons-model-extended-serialization"))
    api(project(":commons:commons-test"))

    implementation(Arrow)
    implementation(CommonsLang3)
    implementation(CommonsIO)
    implementation(HibernateEntityManager)
    implementation(JpaEntityGraph)
    implementation(KotlinStdLib)
    implementation(SpringDataJpa)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
