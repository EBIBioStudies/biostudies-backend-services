import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.MySql
import Projects.SubmissionSubmitter
import SpringBootDependencies.SpringBootConfigurationProcessor
import SpringBootDependencies.SpringBootStarter
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
    id(Plugins.KotlinSpringPlugin) version PluginVersions.KotlinPluginVersion
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))

    api(project(SubmissionSubmitter))
    annotationProcessor(SpringBootConfigurationProcessor)

    implementation(KotlinCoroutines)
    implementation(KotlinLogging)
    implementation(MySql)
    implementation(SpringBootStarter)
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("submission-task")
    archiveVersion.set("1.0.0")
}
