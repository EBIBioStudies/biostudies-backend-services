import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.Guava
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.Poi
import Dependencies.PoiOxml
import Dependencies.SpringWeb
import Dependencies.XlsxStreamer
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.XmlUnitAssertJ
import TestDependencies.XmlUnitCore

dependencies {
    compileOnly(Guava)
    implementation(CommonsIO)
    implementation(Arrow)
    implementation(KotlinStdLib)
    implementation(KotlinReflect)
    implementation(XlsxStreamer)
    implementation(Poi)
    implementation(PoiOxml)
    implementation(CommonsLang3)
    implementation(SpringWeb)
    api(project(":commons:commons-tsv-util"))

    testApi(project(":commons:commons-test"))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
    testImplementation(XmlUnitCore)
    testImplementation(XmlUnitAssertJ)
}
