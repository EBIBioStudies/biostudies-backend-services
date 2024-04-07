import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.RxJava2
import Dependencies.SpringWebFlux
import Projects.CommonsModelExtended
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.SubmissionConfig
import Projects.SubmissionPersistenceCommonApi
import Projects.SubmissionPersistenceSql
import SpringBootDependencies.SpringBootStarterDataJpa
import SpringBootDependencies.SpringBootStarterMail
import SpringBootDependencies.SpringBootStarterWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}


dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    api(project(CommonsModelExtended))
    api(project(SubmissionConfig))
    api(project(SubmissionPersistenceCommonApi))
    api(project(SubmissionPersistenceSql))

    api(project(CommonsTest))
    api(project(CommonsUtil))

    implementation(SpringBootStarterMail)
    implementation(SpringBootStarterWeb)
    implementation(SpringBootStarterDataJpa)
    implementation(SpringWebFlux)

    implementation(Arrow)
    implementation(CommonsIO)
    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(RxJava2)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
