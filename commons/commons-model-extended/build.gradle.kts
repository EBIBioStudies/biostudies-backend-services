import Dependencies.Guava
import Dependencies.JacksonKotlin
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsTest
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    `java-test-fixtures`
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    api(project(CommonsUtil))

    implementation(Guava)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(JacksonKotlin)

    testApi(project(CommonsTest))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
    testImplementation(testFixtures(project(CommonsModelExtendedSerialization)))
}
