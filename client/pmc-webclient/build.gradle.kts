import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Projects.CommonsTest
import Projects.CommonsUtil
import SpringBootDependencies.SpringBootStarterWebFlux
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.Junit5Pioneer
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
    `java-test-fixtures`
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    api(project(CommonsUtil))

    implementation(SpringBootStarterWebFlux)
    implementation(KotlinStdLib)
    implementation(KotlinLogging)
    implementation(KotlinCoroutines)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
    testImplementation(project(CommonsTest))
    testImplementation(Junit5Pioneer)
}
