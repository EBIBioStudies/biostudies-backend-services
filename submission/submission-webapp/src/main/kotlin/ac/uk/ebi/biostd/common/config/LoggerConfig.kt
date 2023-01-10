package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.AppProperties.Companion.CONSUMERS
import ac.uk.ebi.biostd.common.properties.AppProperties.Companion.ENABLE_FIRE
import ac.uk.ebi.biostd.common.properties.AppProperties.Companion.EXECUTE_MONGO_MIGRATIONS
import ac.uk.ebi.biostd.common.properties.AppProperties.Companion.FIRE_TEMP_DIR
import ac.uk.ebi.biostd.common.properties.AppProperties.Companion.FTP_DIR
import ac.uk.ebi.biostd.common.properties.AppProperties.Companion.INSTANCE_BASE_URL
import ac.uk.ebi.biostd.common.properties.AppProperties.Companion.LOGGING_FILE
import ac.uk.ebi.biostd.common.properties.AppProperties.Companion.MAX_CONSUMERS
import ac.uk.ebi.biostd.common.properties.AppProperties.Companion.MONGO_MIGRATION_PACKAGE
import ac.uk.ebi.biostd.common.properties.AppProperties.Companion.REQUEST_FILES_DIR
import ac.uk.ebi.biostd.common.properties.AppProperties.Companion.SUBMISSION_DIR
import ac.uk.ebi.biostd.common.properties.AppProperties.Companion.TEMP_DIR
import ac.uk.ebi.biostd.common.properties.FireAppProperties.Companion.FIRE_HOST
import ac.uk.ebi.biostd.common.properties.FireAppProperties.Companion.FIRE_PASSWORD
import ac.uk.ebi.biostd.common.properties.FireAppProperties.Companion.FIRE_RETRY_INITIAL_INTERVAL
import ac.uk.ebi.biostd.common.properties.FireAppProperties.Companion.FIRE_RETRY_MAX_ATTEMPTS
import ac.uk.ebi.biostd.common.properties.FireAppProperties.Companion.FIRE_RETRY_MAX_INTERVAL
import ac.uk.ebi.biostd.common.properties.FireAppProperties.Companion.FIRE_RETRY_MULTIPLIER
import ac.uk.ebi.biostd.common.properties.FireAppProperties.Companion.FIRE_USERNAME
import ac.uk.ebi.biostd.common.properties.FireAppProperties.Companion.FIRE_VERSION
import ac.uk.ebi.biostd.common.properties.HelperLoggingProperties
import ac.uk.ebi.biostd.common.properties.LoggingProperties
import ac.uk.ebi.biostd.common.properties.SpringProperties
import ac.uk.ebi.biostd.common.properties.MongoProperties
import ac.uk.ebi.biostd.common.properties.MysqlProperties
import ac.uk.ebi.biostd.common.properties.RabbitMQProperties
import ac.uk.ebi.biostd.common.properties.ServletProperties
import ac.uk.ebi.biostd.common.properties.AppProperties
import ac.uk.ebi.biostd.common.properties.FireAppProperties
import ac.uk.ebi.biostd.common.properties.SecurityAppProperties
import ac.uk.ebi.biostd.common.properties.MongoProperties.Companion.MONGO_DB
import ac.uk.ebi.biostd.common.properties.MongoProperties.Companion.MONGO_URI
import ac.uk.ebi.biostd.common.properties.MysqlProperties.Companion.HIBERNATE_NAMING_STRATEGY
import ac.uk.ebi.biostd.common.properties.MysqlProperties.Companion.MYSQL_DRIVER
import ac.uk.ebi.biostd.common.properties.MysqlProperties.Companion.MYSQL_PASSWORD
import ac.uk.ebi.biostd.common.properties.MysqlProperties.Companion.MYSQL_URL
import ac.uk.ebi.biostd.common.properties.MysqlProperties.Companion.MYSQL_USERNAME
import ac.uk.ebi.biostd.common.properties.MysqlProperties.Companion.NEW_ID_GENERATOR_MAPPING
import ac.uk.ebi.biostd.common.properties.MysqlProperties.Companion.SHOW_SQL
import ac.uk.ebi.biostd.common.properties.RabbitMQProperties.Companion.RABBIT_HOST
import ac.uk.ebi.biostd.common.properties.RabbitMQProperties.Companion.RABBIT_LISTENER_DIRECT_PREFETCH
import ac.uk.ebi.biostd.common.properties.RabbitMQProperties.Companion.RABBIT_LISTENER_SIMPLE_PREFETCH
import ac.uk.ebi.biostd.common.properties.RabbitMQProperties.Companion.RABBIT_PASSWORD
import ac.uk.ebi.biostd.common.properties.RabbitMQProperties.Companion.RABBIT_PORT
import ac.uk.ebi.biostd.common.properties.RabbitMQProperties.Companion.RABBIT_USERNAME
import ac.uk.ebi.biostd.common.properties.SecurityAppProperties.Companion.CAPTCHA_KEY
import ac.uk.ebi.biostd.common.properties.SecurityAppProperties.Companion.CHECK_CAPTCHA
import ac.uk.ebi.biostd.common.properties.SecurityAppProperties.Companion.ENVIRONMENT
import ac.uk.ebi.biostd.common.properties.SecurityAppProperties.Companion.FILES_DIR
import ac.uk.ebi.biostd.common.properties.SecurityAppProperties.Companion.INSTANCE_KEY_BETA
import ac.uk.ebi.biostd.common.properties.SecurityAppProperties.Companion.INSTANCE_KEY_DEV
import ac.uk.ebi.biostd.common.properties.SecurityAppProperties.Companion.INSTANCE_KEY_PROD
import ac.uk.ebi.biostd.common.properties.SecurityAppProperties.Companion.MAGIC_DIR
import ac.uk.ebi.biostd.common.properties.SecurityAppProperties.Companion.REQUIRE_ACTIVATION
import ac.uk.ebi.biostd.common.properties.SecurityAppProperties.Companion.TOKEN_HASH
import ac.uk.ebi.biostd.common.properties.ServletProperties.Companion.MAX_HTTP_FORM_SIZE
import ac.uk.ebi.biostd.common.properties.ServletProperties.Companion.MULTIPART_LOCATION
import ac.uk.ebi.biostd.common.properties.ServletProperties.Companion.MULTIPART_MAX_FILE_SIZE
import ac.uk.ebi.biostd.common.properties.ServletProperties.Companion.MULTIPART_MAX_REQUEST_SIZE
import ac.uk.ebi.biostd.common.properties.SpringProperties.Companion.ADMIN_CLIENT_ENABLED
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@Suppress("TooManyFunctions")
@ConditionalOnProperty(prefix = "app", name = ["enablePropertiesLog"], havingValue = "true")
class LoggerConfig {
    @Bean
    fun propertyLogger(
        properties: LoggingProperties
    ): PropertyLogger = PropertyLogger(properties)

    @Bean
    fun loggingProperties(
        properties: HelperLoggingProperties
    ): LoggingProperties = LoggingProperties(properties)

    @Bean
    @Suppress("LongParameterList")
    fun helperLoggingProperties(
        spring: SpringProperties,
        mongo: MongoProperties,
        mysql: MysqlProperties,
        rabbitMQProperties: RabbitMQProperties,
        servlet: ServletProperties,
        app: AppProperties,
        fire: FireAppProperties,
        security: SecurityAppProperties
    ): HelperLoggingProperties =
        HelperLoggingProperties(spring, mongo, mysql, rabbitMQProperties, servlet, app, fire, security)

    @Bean
    fun springProperties(@Value("\${$ADMIN_CLIENT_ENABLED}") adminClientEnable: String): SpringProperties =
        SpringProperties(adminClientEnable)

    @Bean
    fun mongoProperties(
        @Value("\${$MONGO_DB}") database: String,
        @Value("\${$MONGO_URI}") uri: String
    ): MongoProperties = MongoProperties(database, uri)

    @Bean
    @Suppress("LongParameterList")
    fun mysqlProperties(
        @Value("\${$MYSQL_URL}") url: String,
        @Value("\${$MYSQL_USERNAME}") userName: String,
        @Value("\${$MYSQL_PASSWORD}") password: String,
        @Value("\${$MYSQL_DRIVER}") driver: String,
        @Value("\${$SHOW_SQL}") showSql: String,
        @Value("\${$HIBERNATE_NAMING_STRATEGY}") namingStrategy: String,
        @Value("\${$NEW_ID_GENERATOR_MAPPING}") newIDGenerator: String
    ): MysqlProperties = MysqlProperties(url, userName, password, driver, showSql, namingStrategy, newIDGenerator)

    @Bean
    @Suppress("LongParameterList")
    fun rabbitMQProperties(
        @Value("\${$RABBIT_HOST}") host: String,
        @Value("\${$RABBIT_USERNAME}") userName: String,
        @Value("\${$RABBIT_PASSWORD}") password: String,
        @Value("\${$RABBIT_PORT}") port: String,
        @Value("\${$RABBIT_LISTENER_DIRECT_PREFETCH}") directPrefetch: String,
        @Value("\${$RABBIT_LISTENER_SIMPLE_PREFETCH}") simplePrefetch: String
    ): RabbitMQProperties = RabbitMQProperties(host, userName, password, port, directPrefetch, simplePrefetch)

    @Bean
    fun servletProperties(
        @Value("\${$MULTIPART_MAX_FILE_SIZE}") maxFileSize: String,
        @Value("\${$MULTIPART_MAX_REQUEST_SIZE}") maxRequestSize: String,
        @Value("\${$MULTIPART_LOCATION}") multipartLocation: String,
        @Value("\${$MAX_HTTP_FORM_SIZE}") maxFormSize: String
    ): ServletProperties = ServletProperties(maxFileSize, maxRequestSize, multipartLocation, maxFormSize)

    @Bean
    @Suppress("LongParameterList")
    fun appProperties(
        @Value("\${$LOGGING_FILE}") loggingFile: String,
        @Value("\${$CONSUMERS}") consumers: String,
        @Value("\${$MAX_CONSUMERS}") maxConsumers: String,
        @Value("\${$SUBMISSION_DIR}") submissionPath: String,
        @Value("\${$FTP_DIR}") ftpPath: String,
        @Value("\${$TEMP_DIR}") tempDirPath: String,
        @Value("\${$REQUEST_FILES_DIR}") requestFilesPath: String,
        @Value("\${$FIRE_TEMP_DIR}") fireTempDirPath: String,
        @Value("\${$INSTANCE_BASE_URL}") instanceBaseUrl: String,
        @Value("\${$ENABLE_FIRE}") enableFire: String,
        @Value("\${$EXECUTE_MONGO_MIGRATIONS}") executeMigrations: String,
        @Value("\${$MONGO_MIGRATION_PACKAGE}") migrationPackage: String
    ): AppProperties = AppProperties(
        loggingFile,
        consumers,
        maxConsumers,
        submissionPath,
        ftpPath,
        tempDirPath,
        requestFilesPath,
        fireTempDirPath,
        instanceBaseUrl,
        enableFire,
        executeMigrations,
        migrationPackage
    )

    @Bean
    @Suppress("LongParameterList")
    fun fireAppProperties(
        @Value("\${$FIRE_HOST}") host: String,
        @Value("\${$FIRE_USERNAME}") userName: String,
        @Value("\${$FIRE_PASSWORD}") password: String,
        @Value("\${$FIRE_VERSION}") version: String,
        @Value("\${$FIRE_RETRY_MAX_ATTEMPTS}") maxAttempts: String,
        @Value("\${$FIRE_RETRY_INITIAL_INTERVAL}") initialInterval: String,
        @Value("\${$FIRE_RETRY_MULTIPLIER}") multiplier: String,
        @Value("\${$FIRE_RETRY_MAX_INTERVAL}") maxInterval: String
    ): FireAppProperties =
        FireAppProperties(host, userName, password, version, maxAttempts, initialInterval, multiplier, maxInterval)

    @Bean
    @Suppress("LongParameterList")
    fun securityAppProperties(
        @Value("\${$CAPTCHA_KEY}") captchaKey: String,
        @Value("\${$CHECK_CAPTCHA}") checkCaptcha: String,
        @Value("\${$TOKEN_HASH}") tokenHash: String,
        @Value("\${$FILES_DIR}") filesDirPath: String,
        @Value("\${$MAGIC_DIR}") magicDirPath: String,
        @Value("\${$ENVIRONMENT}") environment: String,
        @Value("\${$REQUIRE_ACTIVATION}") requireActivation: String,
        @Value("\${$INSTANCE_KEY_DEV}") instanceKeysDev: String,
        @Value("\${$INSTANCE_KEY_BETA}") instanceKeysBeta: String,
        @Value("\${$INSTANCE_KEY_PROD}") instanceKeysProd: String
    ): SecurityAppProperties = SecurityAppProperties(
        captchaKey,
        checkCaptcha,
        tokenHash,
        filesDirPath,
        magicDirPath,
        environment,
        requireActivation,
        instanceKeysDev,
        instanceKeysBeta,
        instanceKeysProd
    )
}
