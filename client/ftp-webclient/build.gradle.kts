import Dependencies.CommonsNet
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Projects.CommonsUtil
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

    implementation(KotlinStdLib)
    implementation(KotlinLogging)
    implementation(CommonsNet)
}
