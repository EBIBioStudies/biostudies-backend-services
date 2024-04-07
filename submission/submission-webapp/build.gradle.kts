import Dependencies.Arrow
import Dependencies.CommonsFileUpload
import Dependencies.CommonsIO
import Dependencies.CommonsNet
import Dependencies.KotlinCoroutines
import Dependencies.KotlinLogging
import Dependencies.KotlinReflect
import Dependencies.KotlinStdLib
import Dependencies.MySql
import Dependencies.RxJava2
import Dependencies.SpringWebFlux
import Dependencies.SpringfoxSwagger
import Dependencies.SpringfoxSwaggerUI
import Projects.ClientBioWebClient
import Projects.ClientFireWebClient
import Projects.ClusterClient
import Projects.CommonsHttp
import Projects.CommonsModelExtended
import Projects.CommonsModelExtendedSerialization
import Projects.CommonsSerialization
import Projects.CommonsTest
import Projects.CommonsUtil
import Projects.ExcelLibrary
import Projects.FtpWebClient
import Projects.SubmissionNotification
import Projects.SubmissionPersistenceMongo
import Projects.SubmissionPersistenceSql
import Projects.SubmissionSecurity
import Projects.SubmissionSubmitter
import SpringBootDependencies.SpringBootConfigurationProcessor
import SpringBootDependencies.SpringBootStartedAdminClient
import SpringBootDependencies.SpringBootStarterActuator
import SpringBootDependencies.SpringBootStarterAmqp
import SpringBootDependencies.SpringBootStarterConfigProcessor
import SpringBootDependencies.SpringBootStarterDataJpa
import SpringBootDependencies.SpringBootStarterSecurity
import SpringBootDependencies.SpringBootStarterTest
import SpringBootDependencies.SpringBootStarterValidation
import SpringBootDependencies.SpringBootStarterWeb
import SpringBootDependencies.SpringBootStarterWebFlux
import TestDependencies.Awaitility
import TestDependencies.BaseTestCompileDependencies
import TestDependencies.BaseTestRuntimeDependencies
import TestDependencies.FtpServer
import TestDependencies.JsonPathAssert
import TestDependencies.KotlinXmlBuilder
import TestDependencies.SnakeYaml
import TestDependencies.TestContainer
import TestDependencies.TestContainerJUnit
import TestDependencies.TestContainerMongoDb
import TestDependencies.TestContainerMysql
import TestDependencies.TestContainerRabbitMq
import TestDependencies.TestContainerS3mock
import TestDependencies.Wiremock
import TestDependencies.XmlUnitCore
import TestDependencies.XmlUnitMatchers
import TestDependencies.slf4jApi
import TestDependencies.slf4jImp
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id(Plugins.KotlinSpringPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.KotlinJpaPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.KotlinAllOpenPlugin) version PluginVersions.KotlinPluginVersion
    id(Plugins.SpringBootPlugin) version PluginVersions.SpringBootPluginVersion
    id(Plugins.SpringDependencyManagementPlugin) version PluginVersions.SpringDependencyManagementPluginVersion
    id(Plugins.GradleRetry) version PluginVersions.GradleRetryVersion
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.Embeddable")
    annotation("javax.persistence.MappedSuperclass")
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    api(project(ClientFireWebClient))
    api(project(SubmissionPersistenceSql))
    api(project(SubmissionPersistenceMongo))
    api(project(SubmissionSubmitter))
    api(project(SubmissionSecurity))
    api(project(SubmissionNotification))
    api(project(ClusterClient))
    api(project(CommonsModelExtendedSerialization))
    api(project(CommonsSerialization))
    api(project(CommonsUtil))
    api(project(ExcelLibrary))
    api(project(CommonsTest))
    api(project(CommonsHttp))

    annotationProcessor(SpringBootConfigurationProcessor)
    implementation(SpringBootStarterWeb)
    implementation(SpringBootStarterWebFlux)
    implementation(SpringBootStarterAmqp)
    implementation(SpringBootStarterDataJpa)
    implementation(SpringBootStarterConfigProcessor)
    implementation(SpringBootStarterSecurity)
    implementation(SpringBootStarterActuator)
    implementation(SpringBootStarterValidation)
    implementation(SpringBootStartedAdminClient)

    implementation(Arrow)
    implementation(CommonsFileUpload)
    implementation(CommonsNet)
    implementation(CommonsIO)
    implementation(MySql)
    implementation(KotlinReflect)
    implementation(KotlinStdLib)
    implementation(KotlinCoroutines)
    implementation(RxJava2)
    implementation(SpringfoxSwagger)
    implementation(SpringfoxSwaggerUI)
    implementation(SpringWebFlux)
    implementation(KotlinLogging)

    testImplementation(project(ClientBioWebClient))
    testImplementation(testFixtures(project(CommonsSerialization)))
    testImplementation(testFixtures(project(CommonsModelExtended)))

    BaseTestCompileDependencies.forEach { testImplementation(it) }
    BaseTestRuntimeDependencies.forEach { testImplementation(it) }

    testImplementation(SpringBootStarterTest)
    testImplementation(Wiremock)

    testImplementation(slf4jApi)
    testImplementation(slf4jImp)
    testImplementation(FtpServer)

    testImplementation(KotlinXmlBuilder)
    testImplementation(JsonPathAssert)
    testImplementation(Awaitility)
    testImplementation(XmlUnitCore)
    testImplementation(XmlUnitMatchers)
    testImplementation(SnakeYaml)

    testImplementation(TestContainerRabbitMq)
    testImplementation(TestContainerMysql)
    testImplementation(testFixtures(project(FtpWebClient)))
    testImplementation(TestContainerS3mock)
    testImplementation(TestContainerMongoDb)
    testImplementation(TestContainer)
    testImplementation(TestContainerJUnit)
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("submission-webapp")
    archiveVersion.set("1.0.0")
}

sourceSets {
    create("itest") {
        java.srcDir(file("src/itest/java"))
        resources.srcDir(file("src/itest/resources"))
        compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
        runtimeClasspath += output + sourceSets["main"].output + compileClasspath
    }
}

val copySqlSchema = tasks.create<Copy>("copySqlSchema") {
    from("$rootDir/infrastructure/src/main/resources/setup/mysql/Schema.sql")
    into("${project.layout.buildDirectory}/resources/itest")
}

val itest = tasks.create<Test>("itest") {
    dependsOn(copySqlSchema)
    testClassesDirs = sourceSets["itest"].output.classesDirs
    classpath = sourceSets["itest"].runtimeClasspath

    val enableFire = project.property("enableFire")!!
    val enableTask = project.property("enableTaskMode")!!
    val nfsReleaseMode = project.property("nfsReleaseMode")!!
    val includeSecretKey = project.property("includeSecretKey")!!

    println("#####")
    println("Running integration tests with")
    println("fireEnable=$enableFire")
    println("taskMode=$enableTask")
    println("nfsReleaseMode=$nfsReleaseMode")
    println("includeSecretKey=$includeSecretKey")
    println("#######")

    systemProperty("enableFire", enableFire)
    systemProperty("enableTaskMode", enableTask)
    systemProperty("nfsReleaseMode", nfsReleaseMode)
    systemProperty("includeSecretKey", includeSecretKey)

    useJUnitPlatform()
    testLogging.exceptionFormat = TestExceptionFormat.SHORT
    testLogging.showStandardStreams = true
    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("${project.layout.buildDirectory}/jacoco/jacocoITest.exec"))
        classDumpDir = file("${project.layout.buildDirectory}/jacoco/classpathdumps")
    }

    retry {
        maxRetries.set(3)
        maxFailures.set(10)
        failOnPassedAfterRetry.set(false)
    }

    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(testDescriptor: TestDescriptor) {}
        override fun afterTest(desc: TestDescriptor, result: TestResult) {
            val time = result.endTime - result.startTime
            logger.quiet("Executed test ${desc.name} [${desc.className}] with result: ${result.resultType}, in $time ms")
            logger.quiet(result.exception?.stackTraceToString() ?: "Not Register exception")
        }

        override fun afterSuite(suite: TestDescriptor, result: TestResult) {}
    })
}

tasks.getByName<JacocoCoverageVerification>("jacocoTestCoverageVerification") { dependsOn(itest) }
tasks.named<Copy>("processItestResources") { duplicatesStrategy = DuplicatesStrategy.WARN }
