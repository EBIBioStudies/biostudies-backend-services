import Dependencies.MySql
import Dependencies.SpringfoxSwagger
import Dependencies.SpringfoxSwaggerUI
import SpringBootDependencies.SpringBootStartedAdminClient
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterDataJpa
import SpringBootDependencies.SpringBootStarterSecurity
import SpringBootDependencies.SpringBootStarterTest
import SpringBootDependencies.SpringBootStarterWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.H2
import TestDependencies.JsonPathAssert
import TestDependencies.KotlinXmlBuilder
import TestDependencies.XmlUnitCore
import TestDependencies.XmlUnitMatchers
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.41"
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("org.springframework.boot") version "2.1.2.RELEASE"
}

dependencies {
    implementation(project(":submission:persistence"))
    implementation(project(":submission:submitter"))
    implementation(project(":submission:submission-security"))
    implementation(project(":submission:notifications"))
    implementation(project(":commons:commons-serialization"))
    implementation(project(":commons:commons-util"))
    implementation(project(":commons:commons-test"))
    implementation(project(":commons:commons-http"))

    implementation(SpringBootStarterWeb)
    implementation(SpringBootStarterDataJpa)
    implementation(SpringBootStarterConfigProcessor)
    implementation(SpringBootStarterSecurity)

    // Registers the application in the Spring Dashboard
    implementation(SpringBootStartedAdminClient)

    implementation(MySql)
    implementation(SpringfoxSwagger)
    implementation(SpringfoxSwaggerUI)

    testImplementation(project(":client:bio-webclient"))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
    testImplementation(SpringBootStarterTest)
    testImplementation(H2)
    testImplementation(KotlinXmlBuilder)
    testImplementation(JsonPathAssert)
    testImplementation(XmlUnitCore)
    testImplementation(XmlUnitMatchers)
}

apply(from = "$rootDir/gradle/itest.gradle.kts")

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("submission-webapp")
    archiveVersion.set("1.0.0")
}
