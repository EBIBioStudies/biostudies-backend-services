import Dependencies.Arrow
import Dependencies.CommonsCsv
import Dependencies.Guava
import Dependencies.JacksonKotlin
import Dependencies.JacksonXml
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.JsonAssert
import TestDependencies.KotlinXmlBuilder
import TestDependencies.Woodstox
import TestDependencies.XmlUnitAssertJ
import TestDependencies.XmlUnitCore

dependencies {
    api(project(":commons:commons-bio"))
    api(project(":commons:commons-serialization-util"))
    api(project(":commons:commons-test"))
    api(project(":commons:commons-util"))

    implementation(Arrow)
    implementation(Guava)
    implementation(JacksonKotlin)
    implementation(JacksonXml)
    implementation(Woodstox)
    implementation(CommonsCsv)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
    testImplementation(XmlUnitCore)
    testImplementation(XmlUnitAssertJ)
    testImplementation(KotlinXmlBuilder)
    testImplementation(JsonAssert)
}
