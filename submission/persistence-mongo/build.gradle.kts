import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.Guava
import Dependencies.JSONOrg
import Dependencies.KotlinLogging
import Dependencies.KotlinStdLib
import Dependencies.MongockBom
import Dependencies.MongockSpringDataV3
import Dependencies.MongockSpringV5
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
import SpringBootDependencies.SpringBootStarterMongo
import SpringBootDependencies.SpringBootStarterReactiveMongo
import SpringBootDependencies.SpringBootStarterTest
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.TestContainer
import TestDependencies.TestContainerJUnit
import TestDependencies.TestContainerMongoDb
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id(Plugins.KotlinAllOpenPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.KotlinSpringPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion
}

dependencies {
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

    implementation(Arrow)
    implementation(KotlinLogging)
    implementation(CommonsIO)
    implementation(CommonsLang3)
    implementation(Guava)
    implementation(JSONOrg)
    implementation(KotlinLogging)
    implementation(KotlinStdLib)
    implementation(MongockBom)
    implementation(MongockSpringDataV3)
    implementation(MongockSpringV5)
    implementation(SpringBootStarterMongo)
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

tasks.named<BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
