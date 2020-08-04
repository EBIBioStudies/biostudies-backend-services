import Dependencies.Arrow
import Dependencies.ArrowData
import Dependencies.ArrowTypeClasses
import Dependencies.CommonsIO
import Dependencies.KotlinLogging
import Dependencies.RxJava2
import Dependencies.SpringDataJpa
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-bio"))
    api(project(":commons:commons-util"))
    api(project(":commons:commons-serialization"))
    api(project(":submission:submission-security"))

    implementation(Arrow)
    implementation(ArrowTypeClasses)
    implementation(ArrowData)
    implementation(CommonsIO)
    implementation(RxJava2)
    implementation(KotlinLogging)
    implementation(SpringDataJpa)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
