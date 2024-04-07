import Dependencies.Arrow
import Dependencies.ArrowData
import Dependencies.ArrowTypeClasses
import Dependencies.CommonsIO
import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.RxJava2
import Dependencies.SpringWebFlux
import Projects.CommonsBio
import Projects.CommonsHttp
import Projects.CommonsModelExtended
import Projects.CommonsSerialization
import Projects.CommonsUtil
import Projects.SubmissionFileSources
import Projects.SubmissionPersistenceMongo
import Projects.SubmissionSecurity
import SpringBootDependencies.SpringBootStarterAmqp
import SpringBootDependencies.SpringBootStarterDataJpa
import SpringBootDependencies.SpringBootStarterWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.KotlinXmlBuilder
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
    id(Plugins.KotlinSpringPlugin) version PluginVersions.KotlinPluginVersion
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    api(project(CommonsBio))
    api(project(CommonsUtil))
    api(project(CommonsHttp))
    api(project(CommonsSerialization))
    api(project(SubmissionFileSources))
    api(project(SubmissionSecurity))
    api(project(SubmissionPersistenceMongo))

    implementation(Arrow)
    implementation(ArrowTypeClasses)
    implementation(ArrowData)
    implementation(CommonsIO)
    implementation(RxJava2)

    implementation(KotlinCoroutines)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(KotlinLogging)
    implementation(KotlinXmlBuilder)

    implementation(SpringBootStarterDataJpa)
    implementation(SpringBootStarterWeb)
    implementation(SpringBootStarterAmqp)
    implementation(SpringWebFlux)

    testImplementation(SpringBootStarterAmqp)
    testImplementation(testFixtures(project(CommonsModelExtended)))

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
