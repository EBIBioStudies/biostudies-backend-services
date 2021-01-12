import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import SpringBootDependencies.SpringBootStarterMongo
import SpringBootDependencies.SpringBootStarterTest
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.72"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.springframework.boot") version "2.3.2.RELEASE"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.72"
}

dependencies {
    api(project(":commons:commons-util"))
    api(project(":commons:commons-bio"))
    api(project(":commons:commons-serialization"))
    api(project(":commons:commons-model-extended"))
    api(project(":commons:commons-model-extended-mapping"))
    api(project(":submission:submission-config"))
    api(project(":commons:commons-model-extended-serialization"))
    api(project(":commons:commons-test"))
    api(project(":submission:submission-config"))
    api(project(":submission:persistence-common-api"))
    api(project(":submission:persistence-common"))

    implementation(SpringBootStarterMongo)
    implementation(Arrow)
    implementation(CommonsLang3)
    implementation(CommonsIO)
    implementation(KotlinStdLib)
    implementation(KotlinLogging)
    implementation("org.json:json:20201115")

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
    testImplementation(SpringBootStarterTest)
    testImplementation("org.testcontainers:mongodb:1.15.0")
    testImplementation("org.testcontainers:testcontainers:1.15.0")
    testImplementation("org.testcontainers:junit-jupiter:1.15.0")
    testImplementation(SpringBootStarterTest)
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
