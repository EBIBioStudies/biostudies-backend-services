import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.Guava
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.SpringWeb
import Dependencies.XlsxStreamer
import Projects.CommonsTest
import Projects.ExcelLibrary
import Projects.TsvLibrary
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.XmlUnitAssertJ
import TestDependencies.XmlUnitCore

dependencies {
    api(project(TsvLibrary))
    api(project(ExcelLibrary))
    compileOnly(Guava)
    implementation(CommonsIO)
    implementation(Arrow)
    implementation(KotlinLogging)
    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(XlsxStreamer)
    implementation(CommonsLang3)
    implementation(SpringWeb)
    api(project(TsvLibrary))
    api(project(ExcelLibrary))

    testApi(project(CommonsTest))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
    testImplementation(XmlUnitCore)
    testImplementation(XmlUnitAssertJ)
}
