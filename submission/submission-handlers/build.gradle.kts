import Dependencies.JacksonKotlin
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.MySql
import Dependencies.SpringWeb
import Projects.CommonsHttp
import Projects.CommonsModelExtended
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsUtil
import Projects.SubmissionConfig
import Projects.SubmissionNotification
import Projects.SubmissionPersistenceCommonApi
import Projects.SubmissionPersistenceSql
import SpringBootDependencies.SpringBootAmqp
import SpringBootDependencies.SpringBootStarterDataJpa
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.boot") version "2.6.1"
}

dependencies {
    api(project(CommonsModelExtended))
    api(project(CommonsModelExtendedSerialization))
    api(project(CommonsHttp))
    api(project(CommonsUtil))
    api(project(SubmissionNotification))
    api(project(SubmissionConfig))
    api(project(SubmissionPersistenceCommonApi))
    api(project(SubmissionPersistenceSql))

    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(JacksonKotlin)
    implementation(KotlinLogging)
    implementation(MySql)
    implementation(SpringBootAmqp)
    implementation(SpringWeb)
    implementation(SpringBootStarterDataJpa)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("submission-handlers")
    archiveVersion.set("1.0.0")
}
