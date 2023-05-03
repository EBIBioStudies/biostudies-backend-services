import Dependencies.AwsS3
import Dependencies.JSONOrg
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.SpringWeb
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.JsonLibrary
import SpringBootDependencies.SpringRetry
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

plugins {
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
    id("org.springframework.boot") version "2.7.1" apply false
}

the<DependencyManagementExtension>().apply {
    imports {
        mavenBom(BOM_COORDINATES)
    }
}

dependencies {
    api(project(CommonsUtil))

    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(KotlinLogging)
    implementation(JSONOrg)
    implementation(SpringWeb)
    implementation(SpringRetry)
    implementation(AwsS3)

    testApi(project(CommonsTest))
    testApi(project(JsonLibrary))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
