import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import SpringBootDependencies.SpringBootStarter
import Versions.SpringBootVersion

dependencies {
    implementation("$SpringBootStarter:$SpringBootVersion")
    implementation(KotlinStdLib)
    implementation(KotlinReflect)
}
