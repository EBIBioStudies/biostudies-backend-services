import Dependencies.JSONOrg
import Dependencies.KotlinStdLib
import Dependencies.SpringWeb

dependencies {
    api(project(":commons:commons-util"))

    implementation(JSONOrg)
    implementation(KotlinStdLib)
    implementation(SpringWeb)
}
