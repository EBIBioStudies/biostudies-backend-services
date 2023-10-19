import Dependencies.CommonsNet
import Dependencies.JacksonAnnotations
import Dependencies.JacksonCore
import Dependencies.JacksonDataBind
import Dependencies.JacksonKotlin
import Dependencies.JacksonXml
import Dependencies.KMongoAsync
import Dependencies.KMongoCoroutine
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Projects.ClientBioWebClient
import Projects.CommonsModelExtended
import Projects.CommonsModelExtendedMapping
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsSerialization
import Projects.SchedulerTaskProperties
import Projects.SubmissionPersistenceCommonApi
import Projects.SubmissionPersistenceMongo
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterReactiveMongo
import SpringBootDependencies.SpringBootStarterWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.KotlinCoroutinesTest
import TestDependencies.KotlinXmlBuilder
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id(Plugins.KotlinSpringPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    api(project(ClientBioWebClient))
    api(project(CommonsModelExtended))
    api(project(CommonsModelExtendedMapping))
    api(project(CommonsModelExtendedSerialization))
    api(project(CommonsSerialization))
    api(project(SchedulerTaskProperties))
    api(project(SubmissionPersistenceCommonApi))
    api(project(SubmissionPersistenceMongo))

    implementation(CommonsNet)
    implementation(KMongoCoroutine)
    implementation(KMongoAsync)
    implementation(KotlinLogging)
    implementation(KotlinStdLib)
    implementation(JacksonAnnotations)
    implementation(JacksonCore)
    implementation(JacksonDataBind)
    implementation(JacksonKotlin)
    implementation(JacksonXml)
    implementation(SpringBootStarter)
    implementation(SpringBootStarterReactiveMongo)
    implementation(SpringBootStarterWeb)
    implementation(SpringBootStarterConfigProcessor)

    testImplementation(KotlinXmlBuilder)
    testImplementation(KotlinCoroutinesTest)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("exporter-task")
    archiveVersion.set("1.0.0")
}
