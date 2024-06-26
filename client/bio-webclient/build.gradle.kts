import Dependencies.JSONOrg
import Dependencies.KotlinCoroutines
import Dependencies.KotlinCoroutinesReactive
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.ReactorNetty
import Dependencies.SpringWebFlux
import Projects.CommonsBio
import Projects.CommonsHttp
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsSerialization
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    api(project(CommonsUtil))
    api(project(CommonsHttp))
    api(project(CommonsBio))
    api(project(CommonsSerialization))
    api(project(CommonsModelExtendedSerialization))

    implementation(KotlinCoroutines)
    implementation(KotlinCoroutinesReactive)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(JSONOrg)
    implementation(KotlinStdLib)
    implementation(ReactorNetty)
    implementation(SpringWebFlux)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
