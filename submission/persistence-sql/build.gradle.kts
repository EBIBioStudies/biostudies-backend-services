import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.JacksonKotlin
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.SpringAutoConfigure
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
import SpringBootDependencies.SpringBootStarterDataJpa
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.JaxbApi
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

plugins {
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.KotlinJpaPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.KotlinAllOpenPlugin) version PluginVersions.KotlinPluginVersion
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.Embeddable")
    annotation("javax.persistence.MappedSuperclass")
}

the<DependencyManagementExtension>().apply {
    imports {
        mavenBom(BOM_COORDINATES)
    }
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
    implementation(JaxbApi)

    implementation(CommonsIO)
    implementation(CommonsLang3)
    implementation(JacksonKotlin)
    implementation(KotlinLogging)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(SpringAutoConfigure)
    implementation(SpringBootStarterDataJpa)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
