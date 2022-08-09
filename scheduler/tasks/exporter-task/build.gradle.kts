import Dependencies.CommonsNet
import Dependencies.JacksonAnnotations
import Dependencies.JacksonCore
import Dependencies.JacksonDataBind
import Dependencies.JacksonKotlin
import Dependencies.JacksonXml
import Dependencies.KMongoAsync
import Dependencies.KMongoCoroutine
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Projects.ClientBioWebClient
import Projects.CommonsModelExtended
import Projects.CommonsModelExtendedMapping
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsSerialization
import Projects.SchedulerTaskProperties
import Projects.SubmissionPersistenceCommonApi
import Projects.SubmissionPersistenceMongo
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterDataJpa
import SpringBootDependencies.SpringBootStarterMongo
import SpringBootDependencies.SpringBootStarterWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.KotlinXmlBuilder
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.10"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
    id("org.springframework.boot") version "2.2.6.RELEASE"
}

dependencies {
    api(project(ClientBioWebClient))
    api(project(CommonsModelExtended))
    api(project(CommonsModelExtendedMapping))
    api(project(CommonsModelExtendedSerialization))
    api(project(CommonsSerialization))
    api(project(SchedulerTaskProperties))
    api(project(SubmissionPersistenceCommonApi))
    api(project(SubmissionPersistenceMongo))

    implementation(CommonsNet)
    implementation(KMongoCoroutine)
    implementation(KMongoAsync)
    implementation(KotlinLogging)
    implementation(KotlinStdLib)
    implementation(JacksonAnnotations)
    implementation(JacksonCore)
    implementation(JacksonDataBind)
    implementation(JacksonKotlin)
    implementation(JacksonXml)
    implementation(SpringBootStarter)
    implementation(SpringBootStarterMongo)
    implementation(SpringBootStarterDataJpa)
    implementation(SpringBootStarterWeb)
    implementation(SpringBootStarterConfigProcessor)

    testImplementation(KotlinXmlBuilder)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("exporter-task")
    archiveVersion.set("1.0.0")
}
