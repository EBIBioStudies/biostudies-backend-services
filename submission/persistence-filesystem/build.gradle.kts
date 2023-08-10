import Dependencies.Arrow
import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.ZipUtil
import Projects.CommonsBio
import Projects.CommonsModelExtended
import Projects.CommonsModelExtendedMapping
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsSerialization
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.FireWebClient
import Projects.SubmissionPersistenceCommonApi
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

the<DependencyManagementExtension>().apply {
    imports {
        mavenBom(BOM_COORDINATES)
    }
}

dependencies {
    api(project(CommonsBio))
    api(project(CommonsSerialization))
    api(project(CommonsModelExtended))
    api(project(CommonsModelExtendedSerialization))
    api(project(CommonsModelExtendedMapping))
    api(project(CommonsUtil))
    api(project(FireWebClient))
    api(project(SubmissionPersistenceCommonApi))

    implementation(Arrow)
    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(KotlinLogging)
    implementation(ZipUtil)

    testImplementation(project(CommonsTest))
    testImplementation(KotlinCoroutines)
    testImplementation(testFixtures(project(CommonsModelExtendedSerialization)))
    testImplementation(testFixtures(project(CommonsModelExtended)))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
