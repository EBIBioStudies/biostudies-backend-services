import Dependencies.JacksonKotlin
import Dependencies.KotlinLogging
import Dependencies.MySql
import Dependencies.SpringWeb
import SpringBootDependencies.SpringBootAmqp
import SpringBootDependencies.SpringBootStarterDataJpa
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.72"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.springframework.boot") version "2.3.2.RELEASE"
}

dependencies {
    api(project(":commons:commons-model-extended"))
    api(project(":commons:commons-model-extended-serialization"))
    api(project(":commons:commons-http"))
    api(project(":commons:commons-util"))
    api(project(":submission:notifications"))
    api(project(":submission:submission-config"))
    api(project(":submission:persistence-common-api"))
    api(project(":submission:persistence-sql"))

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
