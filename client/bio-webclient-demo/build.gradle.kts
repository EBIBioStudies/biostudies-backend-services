import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.Log4J
import Projects.ClientBioWebClient
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    api(project(ClientBioWebClient))
    implementation(KotlinReflect)
    implementation(KotlinCoroutines)
    implementation(KotlinStdLib)
    implementation(Log4J)
    implementation(KotlinLogging)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
