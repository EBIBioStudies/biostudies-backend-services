import Dependencies.AwsS3
import Dependencies.JSONOrg
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.ReactorNetty
import Dependencies.SpringWebFlux
import Projects.CommonsHttp
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.JsonLibrary
import SpringBootDependencies.SpringRetry
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

the<DependencyManagementExtension>().apply {
    imports {
        mavenBom(BOM_COORDINATES)
    }
}

dependencies {
    api(project(CommonsUtil))
    api(project(CommonsHttp))

    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(KotlinLogging)
    implementation(JSONOrg)
    implementation(SpringWebFlux)
    implementation(ReactorNetty)
    implementation(SpringRetry)
    implementation(AwsS3)

    testApi(project(CommonsTest))
    testApi(project(JsonLibrary))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
