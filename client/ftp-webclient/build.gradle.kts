import Dependencies.CommonsNet
import Dependencies.CommonsPool
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Projects.CommonsTest
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.FtpServer
import TestDependencies.Junit5Pioneer
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

plugins {
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
    id("org.springframework.boot") version "2.7.1" apply false
    `java-test-fixtures`
}

the<DependencyManagementExtension>().apply {
    imports {
        mavenBom(BOM_COORDINATES)
    }
}

dependencies {
    api(project(CommonsUtil))
    implementation(KotlinStdLib)
    implementation(KotlinLogging)
    implementation(CommonsNet)
    implementation(CommonsPool)

    testFixturesImplementation(FtpServer)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
    testImplementation(project(CommonsTest))
    testImplementation(Junit5Pioneer)
}
