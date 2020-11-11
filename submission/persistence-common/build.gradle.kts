import Dependencies.KotlinStdLib
import Dependencies.SpringDataJpa

dependencies {
    api(project(":commons:commons-model-extended"))

    implementation(KotlinStdLib)
    implementation(SpringDataJpa)
}
