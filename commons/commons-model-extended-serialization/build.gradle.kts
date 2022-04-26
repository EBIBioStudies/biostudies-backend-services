import Dependencies.Arrow
import Dependencies.JacksonKotlin
import Dependencies.JacksonXml
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.SpringWeb
import Projects.CommonsModelExtended
import Projects.CommonsModelExtendedTest
import Projects.CommonsSerializationUtil
import Projects.CommonsTest
import Projects.JsonLibrary
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies

plugins {
    `java-test-fixtures`
}

dependencies {
    api(project(CommonsModelExtended))
    api(project(CommonsModelExtendedTest))
    api(project(CommonsSerializationUtil))
    api(project(JsonLibrary))

    implementation(Arrow)
    implementation(JacksonKotlin)
    implementation(JacksonXml)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(SpringWeb)

    testApi(project(CommonsTest))
    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }

    testFixturesApi(Arrow)
}
