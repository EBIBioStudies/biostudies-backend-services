import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.HibernateEntityManager
import Dependencies.JpaEntityGraph
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Dependencies.SpringAutoConfigure
import Dependencies.SpringDataJpa
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

plugins {
    id("org.jetbrains.kotlin.plugin.jpa") version "1.3.72"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.72"
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.Embeddable")
    annotation("javax.persistence.MappedSuperclass")
}

dependencies {
    api(project(":commons:commons-util"))
    api(project(":commons:commons-bio"))
    api(project(":commons:commons-serialization"))
    api(project(":commons:commons-model-extended"))
    api(project(":commons:commons-model-extended-mapping"))
    api(project(":commons:commons-model-extended-serialization"))
    api(project(":commons:commons-test"))
    api(project(":submission:submission-config"))

    api(project(":submission:persistence-common-api"))
    api(project(":submission:persistence-common"))

    implementation(SpringAutoConfigure)
    implementation(SpringDataJpa)
    implementation(Arrow)
    implementation(CommonsLang3)
    implementation(CommonsIO)
    implementation(HibernateEntityManager)
    implementation(JpaEntityGraph)
    implementation(KotlinStdLib)
    implementation(KotlinLogging)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
