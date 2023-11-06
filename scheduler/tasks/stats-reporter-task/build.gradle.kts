
import Dependencies.Arrow
import Dependencies.KotlinCoroutinesReactor
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Projects.SchedulerTaskProperties
import Projects.SubmissionPersistenceCommonApi
import Projects.SubmissionPersistenceMongo
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterReactiveMongo
import SpringBootDependencies.SpringBootStarterTest
import SpringBootDependencies.SpringBootStarterWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.KotlinCoroutinesTest
import TestDependencies.TestContainer
import TestDependencies.TestContainerJUnit
import TestDependencies.TestContainerMongoDb
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id(Plugins.KotlinSpringPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    api(project(SchedulerTaskProperties))
    api(project(SubmissionPersistenceCommonApi))
    api(project(SubmissionPersistenceMongo))

    implementation(Arrow)
    implementation(KotlinCoroutinesReactor)
    implementation(KotlinLogging)
    implementation(KotlinStdLib)
    implementation(SpringBootStarter)
    implementation(SpringBootStarterWeb)
    implementation(SpringBootStarterConfigProcessor)
    implementation(SpringBootStarterReactiveMongo)

    testImplementation(KotlinCoroutinesTest)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
    testImplementation(SpringBootStarterTest) {
        exclude("junit", module = "junit")
    }
    testImplementation(TestContainerMongoDb)
    testImplementation(TestContainer)
    testImplementation(TestContainerJUnit)
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("stats-reporter-task")
    archiveVersion.set("1.0.0")
}
