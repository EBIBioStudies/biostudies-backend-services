import Dependencies.KotlinLogging
import Dependencies.SpringWeb
import SpringBootDependencies.SpringBootAmqp

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.41"
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("org.springframework.boot") version "2.2.6.RELEASE"
}

dependencies {
    implementation(SpringBootAmqp)
    implementation(KotlinLogging)
    implementation(SpringWeb)

    api(project(":commons:commons-model-extended"))
    api(project(":commons:commons-util"))
}
