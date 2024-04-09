import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.Guava
import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.SpringWebFlux
import Dependencies.XlsxStreamer
import Projects.CommonsTest
import Projects.ExcelLibrary
import Projects.TsvLibrary
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.XmlUnitAssertJ
import TestDependencies.XmlUnitCore
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    api(project(TsvLibrary))
    api(project(ExcelLibrary))

    compileOnly(Guava)

    implementation(Arrow)
    implementation(CommonsIO)
    implementation(CommonsLang3)
    implementation(KotlinCoroutines)
    implementation(KotlinLogging)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(SpringWebFlux)
    implementation(XlsxStreamer)

    testApi(project(CommonsTest))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
    testImplementation(XmlUnitCore)
    testImplementation(XmlUnitAssertJ)
}
