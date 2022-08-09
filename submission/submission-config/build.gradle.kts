import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import SpringBootDependencies.SpringBootStarter
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
    implementation(SpringBootStarter)
    implementation(KotlinStdLib)
    implementation(KotlinReflect)
}
