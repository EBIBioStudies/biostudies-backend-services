import Dependencies.KotlinStdLib
import SpringBootDependencies.SpringBootStartedAdmin
import SpringBootDependencies.SpringBootStartedJetty
import SpringBootDependencies.SpringBootStarterWeb
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.boot") version "2.6.1"
    id("java")
}

dependencies {
    implementation(SpringBootStarterWeb)
    implementation(SpringBootStartedJetty)
    implementation(SpringBootStartedAdmin)
    implementation(KotlinStdLib)
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("bio-admin")
    archiveVersion.set("1.0.0")
}
