import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.Guava
import Dependencies.JSONOrg
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Projects.CommonsBio
import Projects.CommonsModelExtended
import Projects.CommonsModelExtendedMapping
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsSerialization
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.SubmissionConfig
import Projects.SubmissionPersistenceCommon
import Projects.SubmissionPersistenceCommonApi
import SpringBootDependencies.SpringBootStarterMongo
import SpringBootDependencies.SpringBootStarterTest
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.TestContainer
import TestDependencies.TestContainerJUnit
import TestDependencies.TestContainerMongoDb
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.72"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.springframework.boot") version "2.3.2.RELEASE"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.72"
}

dependencies {
    api(project(CommonsUtil))
    api(project(CommonsBio))
    api(project(CommonsSerialization))
    api(project(CommonsModelExtended))
    api(project(CommonsModelExtendedMapping))
    api(project(SubmissionConfig))
    api(project(CommonsModelExtendedSerialization))
    api(project(CommonsTest))
    api(project(SubmissionConfig))
    api(project(SubmissionPersistenceCommonApi))
    api(project(SubmissionPersistenceCommon))

    implementation(SpringBootStarterMongo)
    implementation("com.github.cloudyrock.mongock:mongock-bom:4.1.19")
    implementation("com.github.cloudyrock.mongock:mongock-spring-v5:4.1.19")
    implementation("com.github.cloudyrock.mongock:mongodb-springdata-v3-driver:4.1.19")
    implementation(Arrow)
    implementation(Guava)
    implementation(CommonsLang3)
    implementation(CommonsIO)
    implementation(KotlinStdLib)
    implementation(KotlinLogging)
    implementation(JSONOrg)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
    testImplementation(SpringBootStarterTest) {
        exclude("junit", module = "junit")
    }
    testImplementation(TestContainerMongoDb)
    testImplementation(TestContainer)
    testImplementation(TestContainerJUnit)
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
