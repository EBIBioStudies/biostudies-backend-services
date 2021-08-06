import Dependencies.Arrow
import Dependencies.CommonsCsv
import Dependencies.Guava
import Dependencies.JacksonKotlin
import Dependencies.JacksonXml
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsBio
import Projects.CommonsSerializationUtil
import Projects.CommonsTest
import Projects.CommonsUtil
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.JsonAssert
import TestDependencies.KotlinXmlBuilder
import TestDependencies.Woodstox
import TestDependencies.XmlUnitAssertJ
import TestDependencies.XmlUnitCore

dependencies {
    api(project(CommonsBio))
    api(project(CommonsSerializationUtil))
    api(project(CommonsTest))
    api(project(CommonsUtil))

    implementation(KotlinReflect)
    implementation(KotlinStdLib)
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
