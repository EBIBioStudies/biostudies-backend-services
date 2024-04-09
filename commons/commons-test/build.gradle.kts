import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.SpringWebFlux
import Projects.CommonsBio
import Projects.CommonsModelExtended
import TestDependencies.AssertJ
import TestDependencies.JunitExtensions
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    api(project(CommonsBio))
    api(project(CommonsModelExtended))

    implementation(Arrow)
    implementation(AssertJ)
    implementation(CommonsIO)
    implementation(JunitExtensions)
    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(SpringWebFlux)
}
