import Dependencies.Arrow
import Dependencies.CommonsFileUpload
import Dependencies.CommonsIO
import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.MySql
import Dependencies.RxJava2
import Dependencies.SpringfoxSwagger
import Dependencies.SpringfoxSwaggerUI
import Projects.ClientBioWebClient
import Projects.ClientFireWebClient
import Projects.CommonsHttp
import Projects.CommonsModelExtended
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsSerialization
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.ExcelLibrary
import Projects.SubmissionNotification
import Projects.SubmissionPersistenceMongo
import Projects.SubmissionPersistenceSql
import Projects.SubmissionSecurity
import Projects.SubmissionSubmitter
import SpringBootDependencies.SpringBootConfigurationProcessor
import SpringBootDependencies.SpringBootStartedAdminClient
import SpringBootDependencies.SpringBootStarterActuator
import SpringBootDependencies.SpringBootStarterAmqp
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterDataJpa
import SpringBootDependencies.SpringBootStarterSecurity
import SpringBootDependencies.SpringBootStarterTest
import SpringBootDependencies.SpringBootStarterValidation
import SpringBootDependencies.SpringBootStarterWeb
import SpringBootDependencies.SpringRetry
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.JsonPathAssert
import TestDependencies.KotlinXmlBuilder
import TestDependencies.TestContainer
import TestDependencies.TestContainerJUnit
import TestDependencies.TestContainerMongoDb
import TestDependencies.TestContainerMysql
import TestDependencies.Wiremock
import TestDependencies.XmlUnitCore
import TestDependencies.XmlUnitMatchers
import TestDependencies.rabitMqMock
import org.springframework.boot.gradle.tasks.bundling.BootJar

buildscript {
    dependencies {
        "classpath"("org.eclipse.jgit:org.eclipse.jgit:5.13.0.202109080827-r") {
            isForce = true
        }
    }
}

plugins {
    id("com.gorylenko.gradle-git-properties") version "2.4.0-rc1"
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.10"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
    id("org.springframework.boot") version "2.7.1"
    id("org.jetbrains.kotlin.plugin.jpa") version "1.6.10"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.6.10"
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.Embeddable")
    annotation("javax.persistence.MappedSuperclass")
}

dependencies {
    api(project(ClientFireWebClient))
    api(project(SubmissionPersistenceSql))
    api(project(SubmissionPersistenceMongo))
    api(project(SubmissionSubmitter))
    api(project(SubmissionSecurity))
    api(project(SubmissionNotification))
    api(project(CommonsModelExtendedSerialization))
    api(project(CommonsSerialization))
    api(project(CommonsUtil))
    api(project(ExcelLibrary))
    api(project(CommonsTest))
    api(project(CommonsHttp))

    annotationProcessor(SpringBootConfigurationProcessor)

    implementation(SpringBootStarterWeb)
    implementation(SpringBootStarterAmqp)
    implementation(SpringBootStarterDataJpa)
    implementation(SpringBootStarterConfigProcessor)
    implementation(SpringBootStarterSecurity)
    implementation(SpringBootStarterActuator)
    implementation(SpringBootStarterValidation)
    implementation(SpringRetry)
    implementation(SpringBootStartedAdminClient)

    implementation(Arrow)
    implementation(CommonsFileUpload)
    implementation(CommonsIO)
    implementation(MySql)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(KotlinCoroutines)
    implementation(RxJava2)
    implementation(SpringfoxSwagger)
    implementation(SpringfoxSwaggerUI)
    implementation(KotlinLogging)

    testImplementation(project(ClientBioWebClient))
    testImplementation(testFixtures(project(CommonsSerialization)))
    testImplementation(testFixtures(project(CommonsModelExtended)))

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
    testImplementation(SpringBootStarterTest)
    testImplementation(rabitMqMock)
    testImplementation(Wiremock)

    testImplementation(KotlinXmlBuilder)
    testImplementation(JsonPathAssert)
    testImplementation(XmlUnitCore)
    testImplementation(XmlUnitMatchers)

    testImplementation(TestContainerMysql)
    testImplementation(TestContainerMongoDb)
    testImplementation(TestContainer)
    testImplementation(TestContainerJUnit)
}

apply(from = "$rootDir/gradle/itest.gradle.kts")

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("submission-webapp")
    archiveVersion.set("1.0.0")
    dependsOn("generateGitProperties")
}

tasks.named<Copy>("processItestResources") {
    duplicatesStrategy = DuplicatesStrategy.WARN
}
