import Dependencies.CommonsNet
import Dependencies.JacksonXml
import Dependencies.KMongoAsync
import Dependencies.KMongoCoroutine
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Dependencies.SpringWeb
import Projects.ClientBioWebClient
import Projects.CommonsModelExtended
import Projects.CommonsModelExtendedMapping
import Projects.CommonsSerialization
import Projects.SchedulerTaskProperties
import SpringBootDependencies.SpringBootAmqp
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterConfigProcessor
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.72"
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("org.springframework.boot") version "2.2.6.RELEASE"
}

dependencies {
    api(project(ClientBioWebClient))
    api(project(CommonsModelExtended))
    api(project(CommonsModelExtendedMapping))
    api(project(CommonsSerialization))
    api(project(SchedulerTaskProperties))

    implementation(CommonsNet)
    implementation(KMongoCoroutine)
    implementation(KMongoAsync)
    implementation(KotlinLogging)
    implementation(KotlinStdLib)
    implementation(JacksonXml)
    implementation(SpringBootAmqp)
    implementation(SpringBootStarter)
    implementation(SpringWeb)
    implementation(SpringBootStarterConfigProcessor)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("pmc-exporter-task")
    archiveVersion.set("1.0.0")
}
