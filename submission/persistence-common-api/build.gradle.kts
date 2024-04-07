import Dependencies.KotlinCoroutines
import Dependencies.KotlinCoroutinesReactor
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsBio
import Projects.CommonsModelExtended
import SpringBootDependencies.SpringDataCommons
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

plugins {
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion apply false
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
}

dependencies {
    implementation(platform(BOM_COORDINATES))
    api(project(CommonsModelExtended))
    api(project(CommonsBio))

    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(KotlinCoroutines)
    implementation(SpringDataCommons)
    implementation(KotlinCoroutines)
    implementation(KotlinCoroutinesReactor)
}
