import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.Coroutines
import Dependencies.KMongoCoroutine
import Dependencies.KotlinLogging
import Dependencies.OkHttpLogging
import Dependencies.Retrofit2
import Dependencies.SpringWeb
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterConfigProcessor
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.72"
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("org.springframework.boot") version "2.2.6.RELEASE"
}

dependencies {
    implementation(project(":client:bio-webclient"))
    implementation(project(":commons:commons-serialization"))
    implementation(project(":scheduler:task-properties"))
    implementation(project(":commons:commons-http"))

    implementation(Arrow)
    implementation(CommonsIO)
    implementation(CommonsLang3)
    implementation(Coroutines)
    implementation(KMongoCoroutine)
    implementation(KotlinLogging)
    implementation(Retrofit2)
    implementation(OkHttpLogging)

    implementation(SpringBootStarter)
    implementation(SpringWeb)
    implementation(SpringBootStarterConfigProcessor)

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
