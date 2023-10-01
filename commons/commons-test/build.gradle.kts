import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.KotlinCoroutines
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.SpringWebFlux
import Projects.CommonsBio
import Projects.CommonsModelExtended
import TestDependencies.AssertJ
import TestDependencies.JunitExtensions

dependencies {
    api(project(CommonsBio))
    api(project(CommonsModelExtended))

    implementation(Arrow)
    implementation(AssertJ)
    implementation(CommonsIO)
    implementation(JunitExtensions)
    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(KotlinCoroutines)
    implementation(SpringWebFlux)
}
