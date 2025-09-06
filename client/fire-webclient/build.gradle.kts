import Dependencies.AwsS3K
import Dependencies.HttpClientCrt
import Dependencies.JSONOrg
import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsHttp
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.JsonLibrary
import SpringBootDependencies.SpringBootStarterWebFlux
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.KotlinCoroutinesTest
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    api(project(CommonsUtil))
    api(project(CommonsHttp))

    implementation(SpringBootStarterWebFlux)
    implementation(AwsS3K) {
        exclude("com.squareup.okhttp3:okhttp")
    }
    implementation(HttpClientCrt)
    implementation(JSONOrg)
    implementation(KotlinCoroutines)
    implementation(KotlinLogging)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)

    testApi(project(CommonsTest))
    testApi(project(JsonLibrary))
    testImplementation(KotlinCoroutinesTest)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
