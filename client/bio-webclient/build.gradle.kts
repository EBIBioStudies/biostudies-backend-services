import Dependencies.JSONOrg
import Dependencies.SpringWeb

dependencies {
    api(project(":commons:commons-util"))
    api(project(":commons:commons-http"))
    api(project(":commons:commons-bio"))
    api(project(":commons:commons-serialization"))

    implementation(JSONOrg)
    implementation(SpringWeb)
    implementation(kotlin("stdlib"))
}
