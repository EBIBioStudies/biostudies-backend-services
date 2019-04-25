import TestVersions.AssertJVersion
import TestVersions.JunitEngineVersion
import TestVersions.JunitExtensionsVersion
import TestVersions.JunitVersion
import TestVersions.MockKVersion
import TestVersions.XmlUnitVersion
import Versions.CliKtVersion
import Versions.CommonsCliVersion
import Versions.CommonsIOVersion
import Versions.CommonsLang3Version
import Versions.GuavaVersion
import Versions.H2Version
import Versions.HibernateEMVersion
import Versions.JSONOrgVersion
import Versions.JacksonVersion
import Versions.JschVersion
import Versions.JwtVersion
import Versions.KMongoCoroutineVersion
import Versions.KotlinArrowVersion
import Versions.KotlinCoroutineVersion
import Versions.KotlinLoggingVersion
import Versions.KotlinVersion
import Versions.LogbackVersion
import Versions.MySqlVersion
import Versions.Retrofit2CoroutineVersion
import Versions.Retrofit2Version
import Versions.ServletVersion
import Versions.SpringDataVersion
import Versions.SpringVersion
import Versions.WoodstoxVersion
import Versions.XmlBuilderVersion

object TestVersions {
    const val AssertJVersion = "3.11.0"
    const val JunitVersion = "5.4.0"
    const val JunitEngineVersion = "1.2.0"
    const val JunitExtensionsVersion = "2.3.0"
    const val MockKVersion = "1.9"
    const val XmlUnitVersion = "2.6.2"
}

object Versions {
    const val SpringVersion = "5.1.2.RELEASE"
    const val SpringDataVersion = "2.0.9.RELEASE"

    const val KotlinVersion = "1.3.20"
    const val KotlinLoggingVersion = "1.6.20"
    const val KotlinArrowVersion = "0.7.2"
    const val KotlinCoroutineVersion = "1.1.1"
    const val KMongoCoroutineVersion = "3.9.1"

    const val CommonsCliVersion = "1.4"
    const val CommonsLang3Version = "3.8.1"
    const val CommonsIOVersion = "2.6"
    const val MySqlVersion = "6.0.6"
    const val XmlBuilderVersion = "1.4.2"
    const val WoodstoxVersion = "5.1.0"
    const val JacksonVersion = "2.9.8"
    const val LogbackVersion = "1.2.3"
    const val GuavaVersion = "27.0.1-jre"
    const val JwtVersion = "0.9.1"
    const val H2Version = "1.4.197"
    const val ServletVersion = "4.0.1"
    const val HibernateEMVersion = "5.3.5.Final"
    const val JschVersion = "0.1.55"
    const val Retrofit2Version = "2.5.0"
    const val Retrofit2CoroutineVersion = "0.9.2"
    const val JSONOrgVersion = "20090211"
    const val CliKtVersion = "1.7.0"
}

object TestDependencies {
    const val AssertJ = "org.assertj:assertj-core:$AssertJVersion"
    const val MockK = "io.mockk:mockk:$MockKVersion"
    const val KotlinTestJunit = "org.jetbrains.kotlin:kotlin-test-junit:$KotlinVersion"

    const val Junit5Console = "org.junit.platform:junit-platform-console:$JunitEngineVersion"
    const val Junit = "org.junit.jupiter:junit-jupiter:$JunitVersion"
    const val JunitExtensions = "io.github.glytching:junit-extensions:$JunitExtensionsVersion"

    val BaseTestCompileDependencies = arrayOf(Junit, JunitExtensions, AssertJ, MockK, KotlinTestJunit)
    val BaseTestRuntimeDependencies = arrayOf(Junit5Console)

    // Xml related
    const val XmlUnitCore = "org.xmlunit:xmlunit-core:$XmlUnitVersion"
    const val XmlUnitAssertJ = "org.xmlunit:xmlunit-assertj:$XmlUnitVersion"
    const val KotlinXmlBuilder = "org.redundent:kotlin-xml-builder:$XmlBuilderVersion"
    const val Woodstox = "com.fasterxml.woodstox:woodstox-core:$WoodstoxVersion"

    const val H2 = "com.h2database:h2:$H2Version"
}

object Dependencies {

    const val SpringWeb = "org.springframework:spring-web:$SpringVersion"
    const val SpringSecurityCore = "org.springframework.security:spring-security-core:$SpringVersion"
    const val SpringDataJpa = "org.springframework.data:spring-data-jpa:$SpringDataVersion"

    // Web related
    const val JSONOrg = "org.json:json:$JSONOrgVersion"
    const val JacksonKotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:$JacksonVersion"
    const val JacksonCore = "com.fasterxml.jackson.core:jackson-core:$JacksonVersion"
    const val JacksonXml = "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$JacksonVersion"
    const val Retrofit2 = "com.squareup.retrofit2:retrofit:$Retrofit2Version"
    const val RetrofitCoroutine = "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:$Retrofit2CoroutineVersion"

    // Persistence
    const val HibernateEntityManager = "org.hibernate:hibernate-entitymanager:$HibernateEMVersion"
    const val MySql = "mysql:mysql-connector-java:$MySqlVersion"
    const val KMongoCoroutine = "org.litote.kmongo:kmongo-coroutine:$KMongoCoroutineVersion"

    // Misc
    const val ServletApi = "javax.servlet:javax.servlet-api:$ServletVersion"
    const val Logback = "ch.qos.logback:logback-classic:$LogbackVersion"
    const val Jwt = "io.jsonwebtoken:jjwt:$JwtVersion"
    const val Guava = "com.google.guava:guava:$GuavaVersion"
    const val Jsch = "com.jcraft:jsch:$JschVersion"
    const val CliKt = "com.github.ajalt:clikt:$CliKtVersion"


    // Kotlin specific
    const val KotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KotlinVersion"
    const val KotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:$KotlinVersion"
    const val KotlinLogging = "io.github.microutils:kotlin-logging:$KotlinLoggingVersion"
    const val Coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$KotlinCoroutineVersion"

    // Arrow
    const val Arrow = "io.arrow-kt:arrow-core:$KotlinArrowVersion"
    const val ArrowTypeClasses = "io.arrow-kt:arrow-typeclasses:$KotlinArrowVersion"
    const val ArrowData = "io.arrow-kt:arrow-data:$KotlinArrowVersion"

    // Apache
    const val CommonsLang3 = "org.apache.commons:commons-lang3:$CommonsLang3Version"
    const val CommonsIO = "commons-io:commons-io:$CommonsIOVersion"
    const val CommonsCli = "commons-cli:commons-cli:$CommonsCliVersion"
}

object SpringBootDependencies {

    const val SpringBootStarter = "org.springframework.boot:spring-boot-starter"
    const val SpringBootStarterTest = "org.springframework.boot:spring-boot-starter-test"
    const val SpringBootStarterWeb = "org.springframework.boot:spring-boot-starter-web"
    const val SpringBootStarterDataJpa = "org.springframework.boot:spring-boot-starter-data-jpa"
    const val SpringBootStarterConfigProcessor = "org.springframework.boot:spring-boot-configuration-processor"
    const val SpringBootStarterSecurity = "org.springframework.boot:spring-boot-starter-security"
}
