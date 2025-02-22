import Dependencies.AwsS3K
import Dependencies.HttpClientCrt
import Dependencies.JSONOrg
import Dependencies.KotlinCoroutines
import Dependencies.KotlinCoroutinesReactive
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.ReactorNetty
import Dependencies.SpringWebFlux
import Projects.CommonsHttp
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.JsonLibrary
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

    implementation(AwsS3K) {
        exclude("com.squareup.okhttp3:okhttp")
    }
    implementation(HttpClientCrt)
    implementation(JSONOrg)
    implementation(KotlinCoroutines)
    implementation(KotlinCoroutinesReactive)
    implementation(KotlinLogging)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(ReactorNetty)
    implementation(SpringWebFlux)

    testApi(project(CommonsTest))
    testApi(project(JsonLibrary))
    testImplementation(KotlinCoroutinesTest)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
