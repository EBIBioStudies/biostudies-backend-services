import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.KotlinCoroutines
import Dependencies.KotlinCoroutinesReactive
import Dependencies.KotlinLogging
import Dependencies.OkHttpLogging
import Dependencies.Retrofit2
import Dependencies.SpringWebFlux
import Projects.ClientBioWebClient
import Projects.CommonsHttp
import Projects.CommonsSerialization
import Projects.CommonsTest
import Projects.SchedulerTaskProperties
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterReactiveMongo
import SpringBootDependencies.SpringBootStarterTest
import SpringBootDependencies.SpringBootStarterWeb
import TestDependencies.AssertJ
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.TestContainerJUnit
import TestDependencies.TestContainerMongoDb
import TestDependencies.Wiremock
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id(Plugins.KotlinSpringPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    testApi(project(CommonsTest))
    implementation(project(ClientBioWebClient))
    implementation(project(CommonsSerialization))
    implementation(project(SchedulerTaskProperties))
    implementation(project(CommonsHttp))

    implementation(CommonsIO)
    implementation(CommonsLang3)
    implementation(KotlinLogging)
    implementation(Retrofit2)
    implementation(OkHttpLogging)
    implementation(KotlinCoroutines)
    implementation(KotlinCoroutinesReactive)

    implementation(SpringBootStarter)
    implementation(SpringBootStarterReactiveMongo)
    implementation(SpringBootStarterWeb)
    implementation(SpringBootStarterConfigProcessor)
    implementation(SpringWebFlux)

    testImplementation(TestContainerMongoDb)
    testImplementation(SpringBootStarterTest)
    testImplementation(AssertJ)
    testImplementation(Wiremock)
    testImplementation(TestContainerJUnit)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("pmc-processor-task")
    archiveVersion.set("1.0.0")
}
