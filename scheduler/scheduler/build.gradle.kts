import Dependencies.Arrow
import Dependencies.KotlinCoroutines
import Dependencies.KotlinCoroutinesReactor
import Dependencies.KotlinLogging
import Projects.ClusterClient
import Projects.CommonsHttp
import Projects.SchedulerTaskProperties
import SpringBootDependencies.SpringBootStartedAdminClient
import SpringBootDependencies.SpringBootStarter
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterWebFlux
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.KotlinCoroutinesTest
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id(Plugins.KotlinSpringPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

repositories {
    maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
    maven {
        setUrl("https://dl.bintray.com/konrad-kaminski/maven")
    }
    mavenLocal()
}

dependencies {
    api(project(ClusterClient))
    api(project(CommonsHttp))
    api(project(SchedulerTaskProperties))

    implementation(Arrow)
    implementation(KotlinCoroutines)
    implementation(KotlinCoroutinesReactor)
    implementation(KotlinLogging)
    implementation(SpringBootStarter)
    implementation(SpringBootStarterConfigProcessor)
    implementation(SpringBootStartedAdminClient)
    implementation(SpringBootStarterWebFlux)

    testImplementation(KotlinCoroutinesTest)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("scheduler")
    archiveVersion.set("1.0.0")
}
