import Dependencies.JSONOrg
import Dependencies.KotlinStdLib
import Dependencies.SpringWeb

dependencies {
    api(project(":commons:commons-util"))
    api(project(":commons:commons-http"))
    api(project(":commons:commons-bio"))
    api(project(":commons:commons-serialization"))

    implementation(JSONOrg)
    implementation(KotlinStdLib)
    implementation(SpringWeb)
}
