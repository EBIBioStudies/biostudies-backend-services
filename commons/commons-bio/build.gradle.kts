import Dependencies.JacksonDataBind
import Dependencies.JavaValidationApi
import Dependencies.KotlinCoroutines
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsModelExtended
import Projects.CommonsTest
import Projects.CommonsUtil
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
    api(project(CommonsModelExtended))

    implementation(JacksonDataBind)
    implementation(JavaValidationApi)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(KotlinCoroutines)

    testApi(project(CommonsTest))
    testImplementation(KotlinCoroutinesTest)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
