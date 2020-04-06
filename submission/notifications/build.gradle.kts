import Dependencies.KotlinStdLib
import Dependencies.RxJava2
import SpringBootDependencies.SpringBootStarterMail

dependencies {
    implementation("$SpringBootStarterMail:${Versions.SpringBootVersion}")
    implementation(KotlinStdLib)
    implementation(RxJava2)
}
