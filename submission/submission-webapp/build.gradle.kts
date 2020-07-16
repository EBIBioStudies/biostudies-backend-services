import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.JpaEntityGraph
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.MySql
import Dependencies.RxJava2
import Dependencies.SpringfoxSwagger
import Dependencies.SpringfoxSwaggerUI
import SpringBootDependencies.SpringBootAmqp
import SpringBootDependencies.SpringBootStartedAdminClient
import SpringBootDependencies.SpringBootStarterActuator
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
    id("org.springframework.boot") version "2.2.6.RELEASE"
}

dependencies {
    api(project(":submission:persistence"))
    api(project(":submission:submitter"))
    api(project(":submission:submission-security"))
    api(project(":submission:notifications"))
    api(project(":submission:stats"))
    api(project(":commons:commons-model-extended-serialization"))
    api(project(":commons:commons-serialization"))
    api(project(":commons:commons-util"))
    api(project(":commons:commons-test"))
    api(project(":commons:commons-http"))

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation(SpringBootStarterWeb)
    implementation(SpringBootAmqp)
    implementation(SpringBootStarterDataJpa)
    implementation(SpringBootStarterConfigProcessor)
    implementation(SpringBootStarterSecurity)
    implementation(SpringBootStarterActuator)

    // Registers the application in the Spring Dashboard
    implementation(SpringBootStartedAdminClient)

    implementation(Arrow)
    implementation(CommonsIO)
    implementation(MySql)
    implementation(JpaEntityGraph)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(RxJava2)
    implementation(SpringfoxSwagger)
    implementation(SpringfoxSwaggerUI)

    testImplementation(project(":client:bio-webclient"))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
    testImplementation(SpringBootStarterTest)
    testImplementation("com.github.fridujo:rabbitmq-mock:1.1.0")

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
