import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import SpringBootDependencies.SpringBootStarter
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    implementation(platform(BOM_COORDINATES))
    implementation(SpringBootStarter)
    implementation(KotlinStdLib)
    implementation(KotlinReflect)
}
