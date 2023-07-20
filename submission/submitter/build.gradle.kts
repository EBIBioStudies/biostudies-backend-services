import Dependencies.Arrow
import Dependencies.ArrowData
import Dependencies.ArrowTypeClasses
import Dependencies.CommonsIO
import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.RxJava2
import Dependencies.SpringWebFlux
import Projects.CommonsBio
import Projects.CommonsHttp
import Projects.CommonsModelExtended
import Projects.CommonsSerialization
import Projects.CommonsUtil
import Projects.SubmissionFileSources
import Projects.SubmissionSecurity
import SpringBootDependencies.SpringBootStarterAmqp
import SpringBootDependencies.SpringBootStarterDataJpa
import SpringBootDependencies.SpringBootStarterWeb
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
    api(project(CommonsBio))
    api(project(CommonsUtil))
    api(project(CommonsHttp))
    api(project(CommonsSerialization))
    api(project(SubmissionFileSources))
    api(project(SubmissionSecurity))

    implementation(Arrow)
    implementation(ArrowTypeClasses)
    implementation(ArrowData)
    implementation(CommonsIO)
    implementation(RxJava2)

    implementation(KotlinCoroutines)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(KotlinLogging)

    implementation(SpringBootStarterDataJpa)
    implementation(SpringBootStarterWeb)
    implementation(SpringWebFlux)

    testImplementation(SpringBootStarterAmqp)
    testImplementation(testFixtures(project(CommonsModelExtended)))

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
