import Dependencies.MySql
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterDataJpa
import SpringBootDependencies.SpringBootStarterSecurity
import SpringBootDependencies.SpringBootStarterTest
import SpringBootDependencies.SpringBootStarterWeb
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.H2
import TestDependencies.JsonAssert
import TestDependencies.KotlinXmlBuilder
import TestDependencies.XmlUnitCore
import TestDependencies.XmlUnitMatchers
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.41"
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("org.springframework.boot") version "2.1.2.RELEASE"
}

dependencies {
    compile(project(":submission:persistence"))
    compile(project(":submission:submitter"))
    compile(project(":submission:submission-security"))
    compile(project(":submission:notifications"))
    compile(project(":commons:commons-serialization"))
    compile(project(":commons:commons-util"))
    compile(project(":commons:commons-test"))
    compile(project(":commons:commons-http"))

    compile(SpringBootStarterWeb)
    compile(SpringBootStarterDataJpa)
    compile(SpringBootStarterConfigProcessor)
    compile(SpringBootStarterSecurity)

    compile(MySql)

    testCompile(project(":client:bio-webclient"))
    BaseTestCompileDependencies.forEach { testCompile(it) }
    BaseTestRuntimeDependencies.forEach { testCompile(it) }
    testCompile(SpringBootStarterTest)
    testCompile(H2)
    testCompile(KotlinXmlBuilder)
    testCompile(JsonAssert)
    testCompile(XmlUnitCore)
    testCompile(XmlUnitMatchers)
}

apply(from = "$rootDir/gradle/itest.gradle.kts")

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("submission-webapp")
    archiveVersion.set("1.0.0")
}
