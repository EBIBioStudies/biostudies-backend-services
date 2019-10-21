import Dependencies.Arrow
import Dependencies.CommonsIO
import Dependencies.CommonsLang3
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.Poi
import Dependencies.PoiOxml
import Dependencies.XlsxStreamer
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.XmlUnitAssertJ
import TestDependencies.XmlUnitCore

dependencies {
    compile(CommonsIO)
    compile(Arrow)
    compile(KotlinStdLib)
    compile(KotlinReflect)
    compile(XlsxStreamer)

    // Apache commons
    compile(Poi)
    compile(PoiOxml)
    compile(CommonsLang3)

    // General test dependencies
    BaseTestCompileDependencies.forEach { testCompile(it) }
    BaseTestRuntimeDependencies.forEach { testCompile(it) }

    testCompile(XmlUnitCore)
    testCompile(XmlUnitAssertJ)
}
