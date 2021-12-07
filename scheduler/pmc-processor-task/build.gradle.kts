import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.KMongoAsync
import Dependencies.KMongoCoroutine
import Dependencies.KotlinLogging
import Dependencies.MongockBom
import Dependencies.MongockSpringDataV3
import Dependencies.MongockSpringV5
import Dependencies.OkHttpLogging
import Dependencies.Retrofit2
import Dependencies.SpringWeb
import Projects.ClientBioWebClient
import Projects.CommonsHttp
import Projects.CommonsSerialization
import Projects.CommonsTest
import Projects.SchedulerTaskProperties
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterMongo
import SpringBootDependencies.SpringBootStarterTest
import TestDependencies.AssertJ
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.TestContainerJUnit
import TestDependencies.TestContainerMongoDb
import TestDependencies.Wiremock
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.boot") version "2.6.1"
}

dependencies {
    testApi(project(CommonsTest))
    implementation(project(ClientBioWebClient))
    implementation(project(CommonsSerialization))
    implementation(project(SchedulerTaskProperties))
    implementation(project(CommonsHttp))

    implementation(Arrow)
    implementation(CommonsIO)
    implementation(CommonsLang3)
    implementation(KMongoCoroutine)
    implementation(KMongoAsync)
    implementation(KotlinLogging)
    implementation(Retrofit2)
    implementation(OkHttpLogging)

    implementation(MongockBom)
    implementation(MongockSpringV5)
    implementation(MongockSpringDataV3)

    implementation(SpringBootStarter)
    implementation(SpringBootStarterMongo)
    implementation(SpringWeb)
    implementation(SpringBootStarterConfigProcessor)

    testImplementation(TestContainerMongoDb)
    testImplementation(SpringBootStarterTest)
    testImplementation(AssertJ)
    testImplementation(Wiremock)
    testImplementation(TestContainerJUnit)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("pmc-processor-task")
    archiveVersion.set("1.0.0")
}

tasks.named<KotlinCompile>("compileKotlin") {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}
