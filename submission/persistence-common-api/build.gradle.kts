import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.SpringDataJpa
import Projects.CommonsBio
import Projects.CommonsModelExtended

dependencies {
    api(project(CommonsModelExtended))
    api(project(CommonsBio))

    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(SpringDataJpa)
}
