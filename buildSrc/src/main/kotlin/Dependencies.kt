import TestVersions.AssertJVersion
import TestVersions.AwaitilityVersion
import TestVersions.FtpServerVersion
import TestVersions.JaxbApiVersion
import TestVersions.JsonAssertVersion
import TestVersions.JsonPathAssertVersion
import TestVersions.Junit5PioneerVersion
import TestVersions.JunitEngineVersion
import TestVersions.JunitExtensionsVersion
import TestVersions.JunitVersion
import TestVersions.MockKVersion
import TestVersions.RabbitmqMockVersion
import TestVersions.Slf4jVersion
import TestVersions.TestContainerS3mockVersion
import TestVersions.TestContainerVersion
import TestVersions.WiremockVersion
import TestVersions.XmlUnitVersion
import Versions.CliKtVersion
import Versions.CommonsCsvVersion
import Versions.CommonsFileUploadVersion
import Versions.CommonsIOVersion
import Versions.CommonsLang3Version
import Versions.CommonsNetVersion
import Versions.CommonsPoolVersion
import Versions.GuavaVersion
import Versions.H2Version
import Versions.HttpCrtVersion
import Versions.JSONOrgVersion
import Versions.JetBrainsAnnotationsVersion
import Versions.JschVersion
import Versions.JwtVersion
import Versions.KMongoCoroutineVersion
import Versions.KotlinCoroutinesVersion
import Versions.KotlinLoggingVersion
import Versions.KotlinVersion
import Versions.Log4JVersion
import Versions.MongockVersion
import Versions.MySqlVersion
import Versions.OkHttpVersion
import Versions.PoiVersion
import Versions.ReactorNettyVersion
import Versions.Retrofit2Version
import Versions.RxJava2Version
import Versions.S3KVersion
import Versions.S3Version
import Versions.SpringfoxSwaggerVersion
import Versions.WoodstoxVersion
import Versions.XlsxStreamerVersion
import Versions.XmlBuilderVersion
import Versions.ZipUtilVersion

object TestVersions {
    const val AssertJVersion = "3.25.3"
    const val JunitVersion = "5.10.2"
    const val JunitEngineVersion = "1.10.2"
    const val Junit5PioneerVersion = "2.2.0"
    const val JunitExtensionsVersion = "2.3.0"
    const val MockKVersion = "1.13.11"
    const val XmlUnitVersion = "2.6.2"
    const val JaxbApiVersion = "2.3.1"
    const val JsonPathAssertVersion = "2.4.0"
    const val JsonAssertVersion = "1.5.0"
    const val WiremockVersion = "2.27.2"
    const val RabbitmqMockVersion = "1.1.0"
    const val TestContainerVersion = "1.16.2"
    const val TestContainerS3mockVersion = "2.11.0"
    const val AwaitilityVersion = "4.2.1"
    const val FtpServerVersion = "1.2.0"
    const val Slf4jVersion = "2.0.7"
}

object Versions {
    const val KotlinVersion = "1.9.23"
    const val KotlinCoroutinesVersion = "1.8.0"
    const val JetBrainsAnnotationsVersion = "24.0.1"

    const val KotlinLoggingVersion = "3.0.5"
    const val KMongoCoroutineVersion = "4.6.1"
    const val MongockVersion = "4.3.8"

    const val CommonsFileUploadVersion = "1.4"
    const val CommonsLang3Version = "3.8.1"
    const val CommonsIOVersion = "2.6"
    const val CommonsNetVersion = "3.11.1"
    const val CommonsPoolVersion = "2.12.0"
    const val CommonsCsvVersion = "1.8"
    const val MySqlVersion = "8.3.0"
    const val XmlBuilderVersion = "1.7.4"
    const val WoodstoxVersion = "5.1.0"
    const val GuavaVersion = "28.2-jre"
    const val JwtVersion = "0.9.1"
    const val H2Version = "1.4.197"
    const val S3Version = "1.12.772"
    const val S3KVersion = "1.3.83"
    const val HttpCrtVersion = "1.3.26"
    const val OkHttpVersion = "5.0.0-alpha.14"
    const val JschVersion = "0.1.55"
    const val Retrofit2Version = "2.9.0"
    const val JSONOrgVersion = "20201115"
    const val CliKtVersion = "1.7.0"
    const val RxJava2Version = "2.2.8"
    const val PoiVersion = "4.1.0"
    const val XlsxStreamerVersion = "2.1.0"
    const val SpringfoxSwaggerVersion = "2.9.2"
    const val Log4JVersion = "1.7.29"
    const val ZipUtilVersion = "1.15"
    const val ReactorNettyVersion = "1.1.8"
}

object TestDependencies {
    const val AssertJ = "org.assertj:assertj-core:$AssertJVersion"
    const val MockK = "io.mockk:mockk:$MockKVersion"
    const val KotlinTestJunit = "org.jetbrains.kotlin:kotlin-test-junit:$KotlinVersion"
    const val KotlinCoroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$KotlinCoroutinesVersion"

    const val Junit5Pioneer = "org.junit-pioneer:junit-pioneer:$Junit5PioneerVersion"
    const val Junit5Console = "org.junit.platform:junit-platform-console:$JunitEngineVersion"
    const val Junit = "org.junit.jupiter:junit-jupiter:$JunitVersion"
    const val JunitExtensions = "io.github.glytching:junit-extensions:$JunitExtensionsVersion"
    const val rabitMqMock = "com.github.fridujo:rabbitmq-mock:$RabbitmqMockVersion"

    val BaseTestCompileDependencies = arrayOf(Junit, JunitExtensions, AssertJ, MockK, KotlinTestJunit)
    val BaseTestRuntimeDependencies = arrayOf(Junit5Console, KotlinCoroutinesTest)

    // Xml related
    const val JaxbApi = "javax.xml.bind:jaxb-api:$JaxbApiVersion"
    const val XmlUnitCore = "org.xmlunit:xmlunit-core:$XmlUnitVersion"
    const val XmlUnitMatchers = "org.xmlunit:xmlunit-matchers:$XmlUnitVersion"
    const val SnakeYaml = "org.yaml:snakeyaml"
    const val XmlUnitAssertJ = "org.xmlunit:xmlunit-assertj:$XmlUnitVersion"
    const val KotlinXmlBuilder = "org.redundent:kotlin-xml-builder:$XmlBuilderVersion"
    const val Woodstox = "com.fasterxml.woodstox:woodstox-core:$WoodstoxVersion"

    const val H2 = "com.h2database:h2:$H2Version"
    const val Awaitility = "org.awaitility:awaitility:$AwaitilityVersion"
    const val FtpServer = "org.apache.ftpserver:ftpserver:$FtpServerVersion"
    const val slf4jApi = "org.slf4j:slf4j-api:$Slf4jVersion"
    const val slf4jImp = "org.slf4j:slf4j-reload4j:$Slf4jVersion"

    // Json/Http
    const val JsonPathAssert = "com.jayway.jsonpath:json-path-assert:$JsonPathAssertVersion"
    const val JsonAssert = "org.skyscreamer:jsonassert:$JsonAssertVersion"
    const val Wiremock = "com.github.tomakehurst:wiremock-jre8:$WiremockVersion"

    // Test Containers
    const val TestContainerMysql = "org.testcontainers:mysql:$TestContainerVersion"
    const val TestContainerRabbitMq = "org.testcontainers:rabbitmq:$TestContainerVersion"
    const val TestContainerS3mock = "com.adobe.testing:s3mock-testcontainers:$TestContainerS3mockVersion"
    const val TestContainerMongoDb = "org.testcontainers:mongodb:$TestContainerVersion"
    const val TestContainer = "org.testcontainers:testcontainers:$TestContainerVersion"
    const val TestContainerJUnit = "org.testcontainers:junit-jupiter:$TestContainerVersion"
}

object Dependencies {
    const val SpringWebFlux = "org.springframework:spring-webflux"
    const val SpringAutoConfigure = "org.springframework.boot:spring-boot-autoconfigure"

    // Web related
    const val JSONOrg = "org.json:json:$JSONOrgVersion"
    const val JacksonAnnotations = "com.fasterxml.jackson.core:jackson-annotations"
    const val JacksonKotlin = "com.fasterxml.jackson.module:jackson-module-kotlin"
    const val JacksonDataBind = "com.fasterxml.jackson.core:jackson-databind:"
    const val JacksonCore = "com.fasterxml.jackson.core:jackson-core"
    const val JacksonXml = "com.fasterxml.jackson.dataformat:jackson-dataformat-xml"
    const val Retrofit2 = "com.squareup.retrofit2:retrofit:$Retrofit2Version"
    const val OkHttpLogging = "com.squareup.okhttp3:logging-interceptor:$OkHttpVersion"
    const val ReactorNetty = "io.projectreactor.netty:reactor-netty:$ReactorNettyVersion"

    // Persistence
    const val MySql = "com.mysql:mysql-connector-j:$MySqlVersion"
    const val KMongoCoroutine = "org.litote.kmongo:kmongo-coroutine:$KMongoCoroutineVersion"
    const val KMongoAsync = "org.litote.kmongo:kmongo-async:$KMongoCoroutineVersion"
    const val MongockBom = "com.github.cloudyrock.mongock:mongock-bom:$MongockVersion"
    const val MongockSpringV5 = "com.github.cloudyrock.mongock:mongock-spring-v5:$MongockVersion"
    const val MongockSpringDataV3 = "com.github.cloudyrock.mongock:mongodb-springdata-v3-driver:$MongockVersion"

    // Misc
    const val AwsS3K = "aws.sdk.kotlin:s3:$S3KVersion"
    const val HttpClientCrt = "aws.smithy.kotlin:http-client-engine-crt-jvm:$HttpCrtVersion"
    const val OkHttp3 = "com.squareup.okhttp3:okhttp:$OkHttpVersion"
    const val AwsS3 = "com.amazonaws:aws-java-sdk-s3:$S3Version"
    const val ServletApi = "javax.servlet:javax.servlet-api"
    const val Logback = "ch.qos.logback:logback-classic"
    const val Jwt = "io.jsonwebtoken:jjwt:$JwtVersion"
    const val Guava = "com.google.guava:guava:$GuavaVersion"
    const val Jsch = "com.jcraft:jsch:$JschVersion"
    const val CliKt = "com.github.ajalt:clikt:$CliKtVersion"
    const val RxJava2 = "io.reactivex.rxjava2:rxjava:$RxJava2Version"
    const val XlsxStreamer = "com.monitorjbl:xlsx-streamer:$XlsxStreamerVersion"
    const val SpringfoxSwagger = "io.springfox:springfox-swagger2:$SpringfoxSwaggerVersion"
    const val SpringfoxSwaggerUI = "io.springfox:springfox-swagger-ui:$SpringfoxSwaggerVersion"
    const val Log4J = "org.slf4j:slf4j-simple:$Log4JVersion"
    const val JavaValidationApi = "javax.validation:validation-api"
    const val ZipUtil = "org.zeroturnaround:zt-zip:$ZipUtilVersion"

    // Kotlin specific
    const val JetBrainsAnnotations = "org.jetbrains:annotations:$JetBrainsAnnotationsVersion"
    const val KotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KotlinVersion"
    const val KotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:$KotlinVersion"
    const val KotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$KotlinCoroutinesVersion"
    const val KotlinCoroutinesReactive = "org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$KotlinCoroutinesVersion"
    const val KotlinCoroutinesReactor = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$KotlinCoroutinesVersion"
    const val KotlinLogging = "io.github.microutils:kotlin-logging:$KotlinLoggingVersion"

    // Apache
    const val CommonsFileUpload = "commons-fileupload:commons-fileupload:$CommonsFileUploadVersion"
    const val CommonsLang3 = "org.apache.commons:commons-lang3:$CommonsLang3Version"
    const val CommonsIO = "commons-io:commons-io:$CommonsIOVersion"
    const val CommonsNet = "commons-net:commons-net:$CommonsNetVersion"
    const val CommonsPool = "org.apache.commons:commons-pool2:$CommonsPoolVersion"
    const val CommonsCsv = "org.apache.commons:commons-csv:$CommonsCsvVersion"
    const val PoiOxml = "org.apache.poi:poi-ooxml:$PoiVersion"
}

object SpringBootDependencies {
    const val SpringBootStarter = "org.springframework.boot:spring-boot-starter"
    const val SpringBootStarterTest = "org.springframework.boot:spring-boot-starter-test"
    const val SpringBootStarterWeb = "org.springframework.boot:spring-boot-starter-web"
    const val SpringBootStarterWebFlux = "org.springframework.boot:spring-boot-starter-webflux"
    const val SpringBootStarterReactiveMongo = "org.springframework.boot:spring-boot-starter-data-mongodb-reactive"
    const val SpringBootStarterMongo = "org.springframework.boot:spring-boot-starter-data-mongodb"
    const val SpringBootStarterAmqp = "org.springframework.boot:spring-boot-starter-amqp"
    const val SpringBootConfigurationProcessor = "org.springframework.boot:spring-boot-configuration-processor"
    const val SpringBootStarterActuator = "org.springframework.boot:spring-boot-starter-actuator"
    const val SpringBootStarterValidation = "org.springframework.boot:spring-boot-starter-validation"
    const val SpringBootStarterDataJpa = "org.springframework.boot:spring-boot-starter-data-jpa"
    const val SpringBootStarterConfigProcessor = "org.springframework.boot:spring-boot-configuration-processor"
    const val SpringBootStarterSecurity = "org.springframework.boot:spring-boot-starter-security"
    const val SpringBootStarterMail = "org.springframework.boot:spring-boot-starter-mail"
    const val SpringDataCommons = "org.springframework.data:spring-data-commons"
}

object Projects {
    const val JsonLibrary = ":commons:commons-json-util"
    const val TsvLibrary = ":commons:commons-tsv-util"
    const val ExcelLibrary = ":commons:commons-excel-util"
    const val BioWebClient = ":client:bio-webclient"
    const val ClusterClient = ":client:cluster-client"
    const val CommonsUtil = ":commons:commons-util"
    const val CommonsBio = ":commons:commons-bio"
    const val CommonsHttp = ":commons:commons-http"
    const val CommonsSerialization = ":commons:commons-serialization"
    const val CommonsModelExtended = ":commons:commons-model-extended"
    const val CommonsModelExtendedTest = ":commons:commons-model-extended-test"
    const val CommonsModelExtendedMapping = ":commons:commons-model-extended-mapping"
    const val CommonsModelExtendedSerialization = ":commons:commons-model-extended-serialization"
    const val CommonsSerializationUtil = ":commons:commons-serialization-util"
    const val CommonsTest = ":commons:commons-test"
    const val FireWebClient = ":client:fire-webclient"
    const val FtpWebClient = ":client:ftp-webclient"
    const val SubmissionConfig = ":submission:submission-config"
    const val SubmissionFileSources = ":submission:file-sources"
    const val SubmissionPersistenceCommonApi = ":submission:persistence-common-api"
    const val SubmissionPersistenceFilesystem = ":submission:persistence-filesystem"
    const val SubmissionPersistenceSql = ":submission:persistence-sql"
    const val SubmissionPersistenceMongo = ":submission:persistence-mongo"
    const val SubmissionNotification = ":submission:notifications"
    const val SubmissionSecurity = ":submission:submission-security"
    const val SubmissionSubmitter = ":submission:submission-core"
    const val ClientBioWebClient = ":client:bio-webclient"
    const val ClientFireWebClient = ":client:fire-webclient"
    const val SchedulerTaskProperties = ":scheduler:common:task-properties"
    const val EventsPublisher = ":events:events-publisher"
}
