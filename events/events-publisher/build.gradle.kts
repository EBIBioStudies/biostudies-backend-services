import Dependencies.KotlinStdLib
import SpringBootDependencies.SpringBootAmqp
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

plugins {
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.springframework.boot") version "2.3.2.RELEASE"
}

dependencies {
    api(project(":commons:commons-model-extended"))

    implementation(KotlinStdLib)
    implementation(SpringBootAmqp)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
