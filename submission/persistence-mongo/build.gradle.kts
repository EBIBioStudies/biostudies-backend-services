import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.Guava
import Dependencies.JSONOrg
import Dependencies.KotlinCoroutines
import Dependencies.KotlinCoroutinesReactor
import Dependencies.KotlinFlowExtensions
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Projects.CommonsBio
import Projects.CommonsModelExtended
import Projects.CommonsModelExtendedMapping
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsSerialization
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.SubmissionConfig
import Projects.SubmissionPersistenceCommonApi
import Projects.SubmissionPersistenceFilesystem
import SpringBootDependencies.SpringBootStarterReactiveMongo
import SpringBootDependencies.SpringBootStarterTest
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.TestContainer
import TestDependencies.TestContainerJUnit
import TestDependencies.TestContainerMongoDb
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id(Plugins.KotlinAllOpenPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.KotlinSpringPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    api(project(CommonsBio))
    api(project(CommonsModelExtended))
    api(project(CommonsModelExtendedMapping))
    api(project(CommonsModelExtendedSerialization))
    api(project(CommonsSerialization))
    api(project(CommonsTest))
    api(project(CommonsUtil))
    api(project(SubmissionConfig))
    api(project(SubmissionPersistenceFilesystem))
    api(project(SubmissionPersistenceCommonApi))

    implementation(CommonsIO)
    implementation(CommonsLang3)
    implementation(Guava)
    implementation(JSONOrg)
    implementation(KotlinCoroutines)
    implementation(KotlinCoroutinesReactor)
    implementation(KotlinFlowExtensions)
    implementation(KotlinLogging)
    implementation(KotlinStdLib)
    implementation(SpringBootStarterReactiveMongo)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }

    testImplementation(testFixtures(project(CommonsModelExtendedSerialization)))
    testImplementation(testFixtures(project(CommonsModelExtended)))
    testImplementation(SpringBootStarterTest) {
        exclude("junit", module = "junit")
    }
    testImplementation(TestContainerMongoDb)
    testImplementation(TestContainer)
    testImplementation(TestContainerJUnit)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
