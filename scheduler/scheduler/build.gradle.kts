import Dependencies.Arrow
import Dependencies.KotlinLogging
import Projects.CommonsHttp
import Projects.SchedulerClusterClient
import Projects.SchedulerTaskProperties
import SpringBootDependencies.SpringBootStartedAdminClient
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.10"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.springframework.boot") version "2.3.2.RELEASE"
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
    api(project(CommonsHttp))
    api(project(SchedulerClusterClient))
    api(project(SchedulerTaskProperties))

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
