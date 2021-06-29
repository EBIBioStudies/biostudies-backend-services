import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.HibernateEntityManager
import Dependencies.JacksonKotlin
import Dependencies.JpaEntityGraph
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.SpringAutoConfigure
import Dependencies.SpringDataJpa
import Projects.CommonsBio
import Projects.CommonsModelExtended
import Projects.CommonsModelExtendedMapping
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsSerialization
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.SubmissionConfig
import Projects.SubmissionPersistenceFilesystem
import Projects.SubmissionPersistenceCommonApi
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

plugins {
    id("org.jetbrains.kotlin.plugin.jpa") version "1.4.32"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.4.32"
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.Embeddable")
    annotation("javax.persistence.MappedSuperclass")
}

dependencies {
    api(project(CommonsBio))
    api(project(CommonsModelExtended))
    api(project(CommonsModelExtendedMapping))
    api(project(CommonsModelExtendedSerialization))
    api(project(CommonsSerialization))
    api(project(CommonsTest))
    api(project(CommonsUtil))
    api(project(SubmissionConfig))
    api(project(SubmissionPersistenceFilesystem))
    api(project(SubmissionPersistenceCommonApi))

    implementation(Arrow)
    implementation(CommonsIO)
    implementation(CommonsLang3)
    implementation(HibernateEntityManager)
    implementation(JacksonKotlin)
    implementation(JpaEntityGraph)
    implementation(KotlinLogging)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(SpringAutoConfigure)
    implementation(SpringDataJpa)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
