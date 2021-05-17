import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.KMongoAsync
import Dependencies.KMongoCoroutine
import Dependencies.KotlinLogging
import Dependencies.OkHttpLogging
import Dependencies.Retrofit2
import Dependencies.SpringWeb
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterTest
import TestDependencies.AssertJ
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.TestContainerMongoDb
import TestDependencies.Wiremock
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.4.32"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.springframework.boot") version "2.3.2.RELEASE"
}

dependencies {
    testApi(project(":commons:commons-test"))
    implementation(project(":client:bio-webclient"))
    implementation(project(":commons:commons-serialization"))
    implementation(project(":scheduler:task-properties"))
    implementation(project(":commons:commons-http"))

    implementation(Arrow)
    implementation(CommonsIO)
    implementation(CommonsLang3)
    implementation(KMongoCoroutine)
    implementation(KMongoAsync)
    implementation(KotlinLogging)
    implementation(Retrofit2)
    implementation(OkHttpLogging)

    implementation(SpringBootStarter)
    implementation(SpringWeb)
    implementation(SpringBootStarterConfigProcessor)

    testImplementation(TestContainerMongoDb)
    testImplementation(SpringBootStarterTest)
    testImplementation(AssertJ)
    testImplementation(Wiremock)

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
        includeRuntime = true
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}
