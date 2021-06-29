import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.SpringWeb
import TestDependencies.AssertJ
import TestDependencies.Awaitility
import TestDependencies.JunitExtensions

dependencies {
    api(project(":commons:commons-bio"))
    api(project(":commons:commons-model-extended"))

    implementation(Arrow)
    implementation(AssertJ)
    implementation(CommonsIO)
    implementation(JunitExtensions)
    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(SpringWeb)
    implementation(Awaitility)
}
