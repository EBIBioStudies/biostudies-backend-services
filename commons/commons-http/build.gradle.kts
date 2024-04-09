import Dependencies.JacksonCore
import Dependencies.JacksonKotlin
import Dependencies.KotlinCoroutines
import Dependencies.KotlinCoroutinesReactor
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.ServletApi
import Dependencies.SpringWebFlux
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.KotlinCoroutinesTest
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    api(project(CommonsUtil))

    implementation(JacksonKotlin)
    implementation(JacksonCore)
    implementation(SpringWebFlux)
    compileOnly(ServletApi)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(KotlinCoroutines)
    implementation(KotlinCoroutinesReactor)

    testImplementation(KotlinCoroutinesTest)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
