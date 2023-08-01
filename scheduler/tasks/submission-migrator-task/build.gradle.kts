
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Projects.ClientBioWebClient
import Projects.CommonsModelExtended
import Projects.SubmissionPersistenceCommonApi
import Projects.SubmissionPersistenceMongo
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterMongo
import SpringBootDependencies.SpringBootStarterWeb
import TestDependencies.Awaitility
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id(Plugins.KotlinSpringPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    api(project(ClientBioWebClient))
    api(project(CommonsModelExtended))
    api(project(SubmissionPersistenceCommonApi))
    api(project(SubmissionPersistenceMongo))

    implementation(Awaitility)
    implementation(KotlinLogging)
    implementation(KotlinStdLib)
    implementation(SpringBootStarter)
    implementation(SpringBootStarterWeb)
    implementation(SpringBootStarterConfigProcessor)
    implementation(SpringBootStarterMongo)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("submission-migrator-task")
    archiveVersion.set("1.0.0")
}
