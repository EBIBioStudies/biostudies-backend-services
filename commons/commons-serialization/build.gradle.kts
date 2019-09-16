import Dependencies.Arrow
import Dependencies.CommonsCsv
import Dependencies.Guava
import Dependencies.JacksonKotlin
import Dependencies.JacksonXml
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.KotlinXmlBuilder
import TestDependencies.Woodstox
import TestDependencies.XmlUnitAssertJ
import TestDependencies.XmlUnitCore

dependencies {
    compile(project(":commons:commons-test"))
    compile(project(":commons:commons-util"))
    compile(project(":commons:commons-bio"))

    compile(JacksonKotlin)
    compile(JacksonXml)
    compile(Woodstox)
    compile(Guava)
    compile(Arrow)
    compile(CommonsCsv)

    BaseTestCompileDependencies.forEach { testCompile(it) }
    BaseTestRuntimeDependencies.forEach { testCompile(it) }

    testCompile(XmlUnitCore)
    testCompile(XmlUnitAssertJ)
    testCompile(KotlinXmlBuilder)
}
