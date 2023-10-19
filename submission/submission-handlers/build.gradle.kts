import Dependencies.JacksonKotlin
import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.MySql
import Dependencies.SpringWebFlux
import Projects.CommonsHttp
import Projects.CommonsModelExtended
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsUtil
import Projects.SubmissionConfig
import Projects.SubmissionNotification
import Projects.SubmissionPersistenceCommonApi
import Projects.SubmissionPersistenceSql
import SpringBootDependencies.SpringBootStarterAmqp
import SpringBootDependencies.SpringBootStarterDataJpa
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
    api(project(CommonsModelExtended))
    api(project(CommonsModelExtendedSerialization))
    api(project(CommonsHttp))
    api(project(CommonsUtil))
    api(project(SubmissionNotification))
    api(project(SubmissionConfig))
    api(project(SubmissionPersistenceCommonApi))
    api(project(SubmissionPersistenceSql))

    implementation(KotlinCoroutines)
    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(JacksonKotlin)
    implementation(KotlinLogging)
    implementation(MySql)
    implementation(SpringBootStarterAmqp)
    implementation(SpringBootStarterWeb)
    implementation(SpringBootStarterDataJpa)
    implementation(SpringWebFlux)

    testImplementation(KotlinCoroutinesTest)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("submission-handlers")
    archiveVersion.set("1.0.0")
}
