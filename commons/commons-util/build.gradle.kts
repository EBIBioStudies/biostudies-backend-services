import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.Guava
import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.SpringWebFlux
import Dependencies.XlsxStreamer
import Projects.CommonsTest
import Projects.ExcelLibrary
import Projects.TsvLibrary
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.KotlinCoroutinesTest
import TestDependencies.XmlUnitAssertJ
import TestDependencies.XmlUnitCore

dependencies {
    api(project(TsvLibrary))
    api(project(ExcelLibrary))

    compileOnly(Guava)

    implementation(Arrow)
    implementation(CommonsIO)
    implementation(CommonsLang3)
    implementation(KotlinLogging)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(KotlinCoroutines)
    implementation(SpringWebFlux)
    implementation(XlsxStreamer)

    testApi(project(CommonsTest))

    testImplementation(XmlUnitCore)
    testImplementation(XmlUnitAssertJ)
    testImplementation(KotlinCoroutinesTest)
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
}
