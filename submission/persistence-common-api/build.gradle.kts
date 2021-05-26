import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.SpringDataJpa

dependencies {
    api(project(":commons:commons-model-extended"))
    api(project(":commons:commons-bio"))

    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(SpringDataJpa)
}
