import Dependencies.JacksonKotlin
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Dependencies.SpringWeb
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
    api(project(":client:bio-webclient"))
    api(project(":commons:commons-model-extended"))
    api(project(":commons:commons-model-extended-mapping"))
    api(project(":commons:commons-serialization"))
    api(project(":scheduler:task-properties"))

    implementation(KotlinLogging)
    implementation(KotlinStdLib)
    implementation(JacksonKotlin)
    implementation(SpringBootAmqp)
    implementation(SpringBootStarter)
    implementation(SpringWeb)
    implementation(SpringBootStarterConfigProcessor)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("exporter-task")
    archiveVersion.set("1.0.0")
}
