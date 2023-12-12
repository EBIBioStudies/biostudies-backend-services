import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.MySql
import Projects.SubmissionSubmitter
import SpringBootDependencies.SpringBootConfigurationProcessor
import SpringBootDependencies.SpringBootStarter
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
    id(Plugins.KotlinSpringPlugin) version PluginVersions.KotlinPluginVersion
}

the<DependencyManagementExtension>().apply {
    imports {
        mavenBom(BOM_COORDINATES)
    }
}

dependencies {
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
