import Dependencies.Arrow
import Dependencies.CommonsCsv
import Dependencies.Guava
import Dependencies.JacksonKotlin
import Dependencies.JacksonXml
import Dependencies.JetBrainsAnnotations
import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Projects.CommonsBio
import Projects.CommonsSerializationUtil
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.JsonLibrary
import Projects.TsvLibrary
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.JsonAssert
import TestDependencies.KotlinCoroutinesTest
import TestDependencies.KotlinXmlBuilder
import TestDependencies.Woodstox
import TestDependencies.XmlUnitAssertJ
import TestDependencies.XmlUnitCore

plugins {
    `java-test-fixtures`
}

dependencies {
    api(project(CommonsBio))
    api(project(CommonsSerializationUtil))
    api(project(CommonsTest))
    api(project(CommonsUtil))
    api(project(TsvLibrary))
    api(project(JsonLibrary))

    compileOnly(JetBrainsAnnotations)
    implementation(KotlinReflect)
    implementation(KotlinLogging)
    implementation(KotlinStdLib)
    implementation(KotlinCoroutines)
    implementation(Arrow)
    implementation(Guava)
    implementation(JacksonKotlin)
    implementation(JacksonXml)
    implementation(Woodstox)
    implementation(CommonsCsv)

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }
    testImplementation(KotlinCoroutinesTest)
    testImplementation(XmlUnitCore)
    testImplementation(XmlUnitAssertJ)
    testImplementation(KotlinXmlBuilder)
    testImplementation(JsonAssert)
}
