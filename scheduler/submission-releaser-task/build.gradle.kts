import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Dependencies.SpringWeb
import Projects.ClientBioWebClient
import Projects.CommonsModelExtended
import Projects.EventsPublisher
import Projects.SchedulerTaskProperties
import Projects.SubmissionNotification
import SpringBootDependencies.SpringBootAmqp
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterConfigProcessor
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.10"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.springframework.boot") version "2.3.2.RELEASE"
}

dependencies {
    api(project(ClientBioWebClient))
    api(project(CommonsModelExtended))
    api(project(EventsPublisher))
    api(project(SchedulerTaskProperties))
    api(project(SubmissionNotification))

    implementation(KotlinLogging)
    implementation(KotlinStdLib)
    implementation(SpringBootAmqp)
    implementation(SpringBootStarter)
    implementation(SpringWeb)
    implementation(SpringBootStarterConfigProcessor)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("submission-releaser-task")
    archiveVersion.set("1.0.0")
}
