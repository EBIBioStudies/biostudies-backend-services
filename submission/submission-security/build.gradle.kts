import Dependencies.Jwt
import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.Logback
import Dependencies.RxJava2
import Dependencies.ServletApi
import Dependencies.SpringWebFlux
import Projects.ClusterClient
import Projects.CommonsBio
import Projects.CommonsHttp
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.EventsPublisher
import Projects.FtpWebClient
import Projects.SubmissionPersistenceSql
import SpringBootDependencies.SpringBootStarterAmqp
import SpringBootDependencies.SpringBootStarterDataJpa
import SpringBootDependencies.SpringBootStarterSecurity
import SpringBootDependencies.SpringBootStarterWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.JaxbApi
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    api(project(ClusterClient))
    api(project(CommonsUtil))
    api(project(CommonsHttp))
    api(project(CommonsBio))
    api(project(CommonsHttp))
    api(project(EventsPublisher))
    api(project(FtpWebClient))
    api(project(SubmissionPersistenceSql))

    implementation(Jwt)
    implementation(KotlinLogging)
    implementation(JaxbApi)
    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(RxJava2)
    implementation(KotlinCoroutines)
    implementation(SpringBootStarterSecurity)
    implementation(SpringBootStarterDataJpa)
    implementation(SpringBootStarterWeb)
    implementation(SpringWebFlux)
    implementation(ServletApi)

    testApi(project(CommonsTest))
    testImplementation(Logback)
    testImplementation(SpringBootStarterAmqp)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
