import Dependencies.KotlinCoroutines
import Dependencies.KotlinCoroutinesReactive
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Projects.ClientBioWebClient
import Projects.CommonsModelExtended
import Projects.EventsPublisher
import Projects.SchedulerTaskProperties
import Projects.SubmissionPersistenceCommonApi
import Projects.SubmissionPersistenceMongo
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterAmqp
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterReactiveMongo
import SpringBootDependencies.SpringBootStarterWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.KotlinCoroutinesTest
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id(Plugins.KotlinSpringPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    api(project(ClientBioWebClient))
    api(project(CommonsModelExtended))
    api(project(EventsPublisher))
    api(project(SchedulerTaskProperties))
    api(project(SubmissionPersistenceCommonApi))
    api(project(SubmissionPersistenceMongo))

    implementation(KotlinLogging)
    implementation(KotlinStdLib)
    implementation(KotlinCoroutines)
    implementation(KotlinCoroutinesReactive)
    implementation(SpringBootStarterAmqp)
    implementation(SpringBootStarter)
    implementation(SpringBootStarterWeb)
    implementation(SpringBootStarterConfigProcessor)
    implementation(SpringBootStarterReactiveMongo)

    testImplementation(KotlinCoroutinesTest)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("submission-releaser-task")
    archiveVersion.set("1.0.0")
}
