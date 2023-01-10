package ac.uk.ebi.biostd.common.properties

class LoggingProperties(private val helperLoggingProperties: HelperLoggingProperties) {
    val spring = helperLoggingProperties.spring.properties
    val mongo = helperLoggingProperties.mongo.properties
    val mysql = helperLoggingProperties.mysql.properties
    val rabbit = helperLoggingProperties.rabbitMQProperties.properties
    val servlet = helperLoggingProperties.servlet.properties
    val app = helperLoggingProperties.app.properties
    val fire = helperLoggingProperties.fire.properties
    val security = helperLoggingProperties.security.properties
}

@Suppress("LongParameterList")
class HelperLoggingProperties(
    val spring: SpringProperties,
    val mongo: MongoProperties,
    val mysql: MysqlProperties,
    val rabbitMQProperties: RabbitMQProperties,
    val servlet: ServletProperties,
    val app: AppProperties,
    val fire: FireAppProperties,
    val security: SecurityAppProperties
)

class SpringProperties(private val adminClientEnable: String) {
    val properties = mapOf(ADMIN_CLIENT_ENABLED to adminClientEnable)

    companion object {
        const val ADMIN_CLIENT_ENABLED = "spring.boot.admin.client.enabled"
    }
}

class MongoProperties(
    private val database: String,
    private val uri: String
) {
    val properties = mapOf(
        MONGO_DB to database,
        MONGO_URI to uri
    )

    companion object {
        const val MONGO_DB = "spring.data.mongodb.database"
        const val MONGO_URI = "spring.data.mongodb.uri"
    }
}

@Suppress("LongParameterList")
class MysqlProperties(
    private val url: String,
    private val userName: String,
    private val password: String,
    private val driver: String,
    private val showSql: String,
    private val namingStrategy: String,
    private val newIDGenerator: String
) {
    val properties = mapOf(
        MYSQL_URL to url,
        MYSQL_USERNAME to userName,
        MYSQL_PASSWORD to password,
        MYSQL_DRIVER to driver,
        SHOW_SQL to showSql,
        HIBERNATE_NAMING_STRATEGY to namingStrategy,
        NEW_ID_GENERATOR_MAPPING to newIDGenerator
    )

    companion object {
        const val MYSQL_URL = "spring.datasource.url"
        const val MYSQL_USERNAME = "spring.datasource.username"
        const val MYSQL_PASSWORD = "spring.datasource.password"
        const val MYSQL_DRIVER = "spring.datasource.driver-class-name"

        const val SHOW_SQL = "spring.jpa.show-sql"
        const val HIBERNATE_NAMING_STRATEGY = "spring.jpa.hibernate.naming.physical-strategy"
        const val NEW_ID_GENERATOR_MAPPING = "spring.jpa.hibernate.use-new-id-generator-mappings"
    }
}

class RabbitMQProperties(
    private val host: String,
    private val userName: String,
    private val password: String,
    private val port: String,
    private val directPrefetch: String,
    private val simplePrefetch: String
) {
    val properties = mapOf(
        RABBIT_HOST to host,
        RABBIT_USERNAME to userName,
        RABBIT_PASSWORD to password,
        RABBIT_PORT to port,
        RABBIT_LISTENER_DIRECT_PREFETCH to directPrefetch,
        RABBIT_LISTENER_SIMPLE_PREFETCH to simplePrefetch
    )

    companion object {
        const val RABBIT_HOST = "spring.rabbitmq.host"
        const val RABBIT_USERNAME = "spring.rabbitmq.username"
        const val RABBIT_PASSWORD = "spring.rabbitmq.password"
        const val RABBIT_PORT = "spring.rabbitmq.port"
        const val RABBIT_LISTENER_DIRECT_PREFETCH = "spring.rabbitmq.listener.direct.prefetch"
        const val RABBIT_LISTENER_SIMPLE_PREFETCH = "spring.rabbitmq.listener.simple.prefetch"
    }
}

class ServletProperties(
    private val maxFileSize: String,
    private val maxRequestSize: String,
    private val multipartLocation: String,
    private val maxFormSize: String
) {
    val properties = mapOf(
        MULTIPART_MAX_FILE_SIZE to maxFileSize,
        MULTIPART_MAX_REQUEST_SIZE to maxRequestSize,
        MULTIPART_LOCATION to multipartLocation,
        MAX_HTTP_FORM_SIZE to maxFormSize
    )

    companion object {
        const val MULTIPART_MAX_FILE_SIZE = "spring.servlet.multipart.max-file-size"
        const val MULTIPART_MAX_REQUEST_SIZE = "spring.servlet.multipart.max-request-size"
        const val MULTIPART_LOCATION = "spring.servlet.multipart.location"
        const val MAX_HTTP_FORM_SIZE = "server.tomcat.max-http-form-post-size"
    }
}

@Suppress("LongParameterList")
class AppProperties(
    private val loggingFile: String,
    private val consumers: String,
    private val maxConsumers: String,
    private val submissionPath: String,
    private val ftpPath: String,
    private val tempDirPath: String,
    private val requestFilesPath: String,
    private val fireTempDirPath: String,
    private val instanceBaseUrl: String,
    private val enableFire: String,
    private val executeMigrations: String,
    private val migrationPackage: String
) {
    val properties = mapOf(
        LOGGING_FILE to loggingFile,
        CONSUMERS to consumers,
        MAX_CONSUMERS to maxConsumers,
        SUBMISSION_DIR to submissionPath,
        FTP_DIR to ftpPath,
        TEMP_DIR to tempDirPath,
        REQUEST_FILES_DIR to requestFilesPath,
        FIRE_TEMP_DIR to fireTempDirPath,
        INSTANCE_BASE_URL to instanceBaseUrl,
        ENABLE_FIRE to enableFire,
        EXECUTE_MONGO_MIGRATIONS to executeMigrations,
        MONGO_MIGRATION_PACKAGE to migrationPackage
    )

    companion object {
        const val LOGGING_FILE = "logging.file.name"
        const val CONSUMERS = "app.consumers"
        const val MAX_CONSUMERS = "app.maxConsumers"
        const val SUBMISSION_DIR = "app.submissionPath"
        const val FTP_DIR = "app.ftpPath"
        const val TEMP_DIR = "app.tempDirPath"
        const val REQUEST_FILES_DIR = "app.requestFilesPath"
        const val FIRE_TEMP_DIR = "app.fireTempDirPath"
        const val INSTANCE_BASE_URL = "app.instanceBaseUrl"
        const val ENABLE_FIRE = "app.persistence.enableFire"
        const val EXECUTE_MONGO_MIGRATIONS = "app.mongo.execute-migrations"
        const val MONGO_MIGRATION_PACKAGE = "app.mongo.migration-package"
    }
}

@Suppress("LongParameterList")
class FireAppProperties(
    private val host: String,
    private val userName: String,
    private val password: String,
    private val version: String,
    private val maxAttempts: String,
    private val initialInterval: String,
    private val multiplier: String,
    private val maxInterval: String
) {
    val properties = mapOf(
        FIRE_HOST to host,
        FIRE_USERNAME to userName,
        FIRE_PASSWORD to password,
        FIRE_VERSION to version,
        FIRE_RETRY_MAX_ATTEMPTS to maxAttempts,
        FIRE_RETRY_INITIAL_INTERVAL to initialInterval,
        FIRE_RETRY_MULTIPLIER to multiplier,
        FIRE_RETRY_MAX_INTERVAL to maxInterval
    )

    companion object {
        const val FIRE_HOST = "app.fire.host"
        const val FIRE_USERNAME = "app.fire.username"
        const val FIRE_PASSWORD = "app.fire.password"
        const val FIRE_VERSION = "app.fire.version"
        const val FIRE_RETRY_MAX_ATTEMPTS = "app.fire.retry.maxAttempts"
        const val FIRE_RETRY_INITIAL_INTERVAL = "app.fire.retry.initialInterval"
        const val FIRE_RETRY_MULTIPLIER = "app.fire.retry.multiplier"
        const val FIRE_RETRY_MAX_INTERVAL = "app.fire.retry.maxInterval"
    }
}

@Suppress("LongParameterList")
class SecurityAppProperties(
    private val captchaKey: String,
    private val checkCaptcha: String,
    private val tokenHash: String,
    private val filesDirPath: String,
    private val magicDirPath: String,
    private val environment: String,
    private val requireActivation: String,
    private val instanceKeysDev: String,
    private val instanceKeysBeta: String,
    private val instanceKeysProd: String
) {
    val properties = mapOf(
        CAPTCHA_KEY to captchaKey,
        CHECK_CAPTCHA to checkCaptcha,
        TOKEN_HASH to tokenHash,
        FILES_DIR to filesDirPath,
        MAGIC_DIR to magicDirPath,
        ENVIRONMENT to environment,
        REQUIRE_ACTIVATION to requireActivation,
        INSTANCE_KEY_DEV to instanceKeysDev,
        INSTANCE_KEY_BETA to instanceKeysBeta,
        INSTANCE_KEY_PROD to instanceKeysProd
    )

    companion object {
        const val CAPTCHA_KEY = "app.security.captchaKey"
        const val CHECK_CAPTCHA = "app.security.checkCaptcha"
        const val TOKEN_HASH = "app.security.tokenHash"
        const val FILES_DIR = "app.security.filesDirPath"
        const val MAGIC_DIR = "app.security.magicDirPath"
        const val ENVIRONMENT = "app.security.environment"
        const val REQUIRE_ACTIVATION = "app.security.requireActivation"
        const val INSTANCE_KEY_DEV = "app.security.instanceKeys.dev"
        const val INSTANCE_KEY_BETA = "app.security.instanceKeys.beta"
        const val INSTANCE_KEY_PROD = "app.security.instanceKeys.prod"
    }
}
