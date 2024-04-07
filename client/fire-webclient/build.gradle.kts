import Dependencies.AwsS3
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

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}



dependencies {
    api(project(CommonsUtil))
    api(project(CommonsHttp))

    implementation(AwsS3)
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
