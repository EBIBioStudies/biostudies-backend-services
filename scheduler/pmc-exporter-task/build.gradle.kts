import Dependencies.CommonsNet
import Dependencies.JacksonXml
import Dependencies.KMongoAsync
import Dependencies.KMongoCoroutine
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Dependencies.SpringDataJpa
import Dependencies.SpringWeb
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsSerialization
import Projects.SchedulerTaskProperties
import Projects.SubmissionPersistenceCommonApi
import Projects.SubmissionPersistenceMongo
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterMongo
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.72"
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("org.springframework.boot") version "2.2.6.RELEASE"
}

dependencies {
    api(project(CommonsSerialization))
    api(project(CommonsModelExtendedSerialization))
    api(project(SubmissionPersistenceCommonApi))
    api(project(SubmissionPersistenceMongo))
    api(project(SchedulerTaskProperties))

    implementation(CommonsNet)
    implementation(KMongoCoroutine)
    implementation(KMongoAsync)
    implementation(KotlinLogging)
    implementation(KotlinStdLib)
    implementation(JacksonXml)
    implementation(SpringDataJpa)
    implementation(SpringBootStarter)
    implementation(SpringBootStarterMongo)
    implementation(SpringWeb)
    implementation(SpringBootStarterConfigProcessor)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("pmc-exporter-task")
    archiveVersion.set("1.0.0")
}
