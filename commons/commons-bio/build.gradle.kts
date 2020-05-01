import Dependencies.Arrow
import Dependencies.JacksonDataBind
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

dependencies {
    api(project(":commons:commons-util"))

    implementation(kotlin("stdlib"))
    implementation(Arrow)
    implementation(JacksonDataBind)
//    implementation(SpringBootStarterWeb)
//    implementation(SpringBootStarterDataJpa)
//    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("javax.validation:validation-api:2.0.1.Final")

    testApi(project(":commons:commons-test"))

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
