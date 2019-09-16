import Dependencies.Coroutines
import Dependencies.KMongoCoroutine
import Dependencies.KotlinLogging
import Dependencies.Retrofit2
import Dependencies.RetrofitCoroutine
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterConfigProcessor
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.41"
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("org.springframework.boot") version "2.1.1.RELEASE"
}

dependencies {
    implementation(project(":client:bio-webclient"))
    implementation(project(":commons:commons-serialization"))
    implementation(project(":scheduler:task-properties"))
    implementation(project(":commons:commons-http"))

    implementation(SpringBootStarter)
    implementation(SpringBootStarterConfigProcessor)

    implementation(KMongoCoroutine)
    implementation(Coroutines)
    implementation(KotlinLogging)

    implementation(Retrofit2)
    implementation(RetrofitCoroutine)

    // Junit dependencies
    BaseTestCompileDependencies.forEach { testCompile(it) }
    BaseTestRuntimeDependencies.forEach { testCompile(it) }
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
