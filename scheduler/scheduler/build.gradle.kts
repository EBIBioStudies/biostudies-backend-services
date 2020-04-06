import Dependencies.Arrow
import Dependencies.KotlinLogging
import SpringBootDependencies.SpringBootStartedAdminClient
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.41"
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("org.springframework.boot") version "2.1.1.RELEASE"
}

repositories {
    jcenter()
    maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
    maven {
        setUrl("https://dl.bintray.com/konrad-kaminski/maven")
    }
    mavenLocal()
}

dependencies {
    api(project(":scheduler:cluster-client"))
    api(project(":scheduler:task-properties"))
    api(project(":commons:commons-http"))

    implementation(Arrow)
    implementation(KotlinLogging)
    implementation(SpringBootStarter)
    implementation(SpringBootStarterConfigProcessor)
    implementation(SpringBootStartedAdminClient)
    implementation(SpringBootStarterWeb)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("scheduler")
    archiveVersion.set("1.0.0")
}
