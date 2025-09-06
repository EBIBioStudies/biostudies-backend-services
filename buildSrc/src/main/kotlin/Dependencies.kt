import TestVersions.AwaitilityVersion
import TestVersions.FtpServerVersion
import TestVersions.JaxbApiVersion
import TestVersions.JsonAssertVersion
import TestVersions.JsonPathAssertVersion
import TestVersions.Junit5PioneerVersion
import TestVersions.JunitExtensionsVersion
import TestVersions.MockKVersion
import TestVersions.TestContainerS3mockVersion
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
import Versions.HttpCrtVersion
import Versions.JSONOrgVersion
import Versions.JetBrainsAnnotationsVersion
import Versions.JschVersion
import Versions.JwtVersion
import Versions.KotlinCoroutinesVersion
import Versions.KotlinFlowExtensionsVersion
import Versions.KotlinLoggingVersion
import Versions.KotlinVersion
import Versions.Log4JVersion
import Versions.MySqlVersion
import Versions.OkHttpVersion
import Versions.PoiVersion
import Versions.Retrofit2Version
import Versions.RxJava2Version
import Versions.S3KVersion
import Versions.S3Version
import Versions.SpringfoxSwaggerVersion
import Versions.WoodstoxVersion
import Versions.XlsxStreamerVersion
import Versions.XmlBuilderVersion
import Versions.ZeroAllocationHashingVersion
import Versions.ZipUtilVersion

object TestVersions {
    const val JunitExtensionsVersion = "2.6.0"
    const val Junit5PioneerVersion = "2.3.0"
    const val MockKVersion = "1.13.11"
    const val XmlUnitVersion = "2.6.2"
    const val JaxbApiVersion = "2.3.1"
    const val JsonPathAssertVersion = "2.4.0"
    const val JsonAssertVersion = "1.5.0"
    const val WiremockVersion = "3.13.1"
    const val TestContainerS3mockVersion = "2.11.0"
    const val AwaitilityVersion = "4.2.1"
    const val FtpServerVersion = "1.2.0"
}

object Versions {
    const val KotlinVersion = "1.9.23"
    const val KotlinCoroutinesVersion = "1.8.0"
    const val JetBrainsAnnotationsVersion = "24.0.1"
    const val KotlinLoggingVersion = "3.0.5"
    const val KotlinFlowExtensionsVersion = "0.0.14"

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
    const val S3Version = "1.12.772"
    const val S3KVersion = "1.3.83"
    const val HttpCrtVersion = "1.3.26"
    const val OkHttpVersion = "5.0.0-alpha.14"
    const val JschVersion = "0.2.25"
    const val Retrofit2Version = "2.9.0"
    const val JSONOrgVersion = "20201115"
    const val CliKtVersion = "1.7.0"
    const val RxJava2Version = "2.2.8"
    const val PoiVersion = "4.1.0"
    const val XlsxStreamerVersion = "2.1.0"
    const val SpringfoxSwaggerVersion = "2.9.2"
    const val Log4JVersion = "1.7.29"
    const val ZipUtilVersion = "1.15"
    const val ZeroAllocationHashingVersion = "0.16"
}

object TestDependencies {
    const val AssertJ = "org.assertj:assertj-core"
    const val MockK = "io.mockk:mockk:$MockKVersion"
    const val KotlinTestJunit = "org.jetbrains.kotlin:kotlin-test-junit:$KotlinVersion"
    const val KotlinCoroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$KotlinCoroutinesVersion"

    const val Junit5Pioneer = "org.junit-pioneer:junit-pioneer:$Junit5PioneerVersion"
    const val Junit5Console = "org.junit.platform:junit-platform-console"
    const val Junit = "org.junit.jupiter:junit-jupiter"
    const val JunitExtensions = "io.github.glytching:junit-extensions:$JunitExtensionsVersion"

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

    const val Awaitility = "org.awaitility:awaitility:$AwaitilityVersion"
    const val FtpServer = "org.apache.ftpserver:ftpserver:$FtpServerVersion"
    const val slf4jApi = "org.slf4j:slf4j-api"

    // Json/Http
    const val JsonPathAssert = "com.jayway.jsonpath:json-path-assert:$JsonPathAssertVersion"
    const val JsonAssert = "org.skyscreamer:jsonassert:$JsonAssertVersion"
    const val Wiremock = "org.wiremock:wiremock-standalone:$WiremockVersion"

    // Test Containers
    const val TestContainerMysql = "org.testcontainers:mysql"
    const val TestContainerRabbitMq = "org.testcontainers:rabbitmq"
    const val TestContainerS3mock = "com.adobe.testing:s3mock-testcontainers:$TestContainerS3mockVersion"
    const val TestContainerMongoDb = "org.testcontainers:mongodb"
    const val TestContainer = "org.testcontainers:testcontainers"
    const val TestContainerJUnit = "org.testcontainers:junit-jupiter"
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
    const val ReactorNetty = "io.projectreactor.netty:reactor-netty"

    // Persistence
    const val MySql = "com.mysql:mysql-connector-j:$MySqlVersion"

    // Misc
    const val AwsS3K = "aws.sdk.kotlin:s3:$S3KVersion"
    const val HttpClientCrt = "aws.smithy.kotlin:http-client-engine-crt-jvm:$HttpCrtVersion"
    const val OkHttp3 = "com.squareup.okhttp3:okhttp:$OkHttpVersion"
    const val AwsS3 = "com.amazonaws:aws-java-sdk-s3:$S3Version"
    const val ServletApi = "jakarta.servlet:jakarta.servlet-api"
    const val Logback = "ch.qos.logback:logback-classic"
    const val Jwt = "io.jsonwebtoken:jjwt:$JwtVersion"
    const val Guava = "com.google.guava:guava:$GuavaVersion"
    const val Jsch = "com.github.mwiede:jsch:$JschVersion"
    const val CliKt = "com.github.ajalt:clikt:$CliKtVersion"
    const val RxJava2 = "io.reactivex.rxjava2:rxjava:$RxJava2Version"
    const val XlsxStreamer = "com.monitorjbl:xlsx-streamer:$XlsxStreamerVersion"
    const val SpringfoxSwagger = "io.springfox:springfox-swagger2:$SpringfoxSwaggerVersion"
    const val SpringfoxSwaggerUI = "io.springfox:springfox-swagger-ui:$SpringfoxSwaggerVersion"
    const val Log4J = "org.slf4j:slf4j-simple:$Log4JVersion"
    const val JavaValidationApi = "jakarta.validation:jakarta.validation-api"
    const val ZipUtil = "org.zeroturnaround:zt-zip:$ZipUtilVersion"
    const val ZeroAllocationHashing = "net.openhft:zero-allocation-hashing:$ZeroAllocationHashingVersion"

    // Kotlin specific
    const val JetBrainsAnnotations = "org.jetbrains:annotations:$JetBrainsAnnotationsVersion"
    const val KotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KotlinVersion"
    const val KotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:$KotlinVersion"
    const val KotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$KotlinCoroutinesVersion"
    const val KotlinCoroutinesReactive = "org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$KotlinCoroutinesVersion"
    const val KotlinCoroutinesReactor = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$KotlinCoroutinesVersion"
    const val KotlinLogging = "io.github.microutils:kotlin-logging:$KotlinLoggingVersion"
    const val KotlinFlowExtensions = "com.github.akarnokd:kotlin-flow-extensions:$KotlinFlowExtensionsVersion"

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
