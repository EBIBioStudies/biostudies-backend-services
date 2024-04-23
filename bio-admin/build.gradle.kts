import Dependencies.KotlinStdLib
import SpringBootDependencies.SpringBootStarterWeb
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id(Plugins.KotlinSpringPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    implementation(SpringBootStarterWeb)
    implementation(KotlinStdLib)
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("bio-admin")
    archiveVersion.set("1.0.0")
}
