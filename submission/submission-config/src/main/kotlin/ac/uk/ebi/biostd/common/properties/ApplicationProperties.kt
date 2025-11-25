package ac.uk.ebi.biostd.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class ApplicationProperties(
    val processId: String,
    val retrySubmission: Boolean,
    val asyncMode: Boolean,
    val instanceBaseUrl: String,
    val subBasePath: String?,
    val consumers: Int,
    val maxConsumers: Int,
    val migrationProperties: MigrationProperties,
    val security: SecurityProperties,
    val fire: FireProperties,
    val validator: ValidatorProperties,
    val persistence: PersistenceProperties,
    val notifications: SubmissionNotificationsProperties,
    val doi: DoiProperties,
    val submissionTask: SubmissionTaskProperties,
    val cluster: ClusterProperties,
)

data class RetryProperties(
    val maxAttempts: Int,
    val initialInterval: Long,
    val multiplier: Double,
    val maxInterval: Long,
)

data class FireProperties(
    val host: String,
    val version: String,
    val username: String,
    val password: String,
    val tempDirPath: String,
    val retry: RetryProperties,
    val s3: S3Properties,
)

data class S3Properties(
    val accessKey: String,
    val secretKey: String,
    val region: String,
    val endpoint: String,
    val bucket: String,
)

data class MigrationProperties(
    val enableMigration: Boolean = false,
    val user: String = "biostudies-dev@ebi.ac.uk",
    val limit: Int = 10,
    val modifiedBeforeDays: Int = 360,
)

data class ValidatorProperties(
    val euToxRiskValidationApi: String,
)

data class PersistenceProperties(
    val concurrency: Int,
    val enableFire: Boolean = false,
    val includeSecretKey: Boolean = false,
    val nfsReleaseMode: String,
    val pageTabFallbackPath: String,
    val privateSubmissionsPath: String,
    val publicSubmissionsPath: String,
    val privateSubmissionFtpOutPath: String,
    var publicSubmissionFtpOutPath: String,
    val requestFilesPath: String,
    val tempDirPath: String,
)

data class SubmissionNotificationsProperties(
    val requestQueue: String,
    val requestRoutingKey: String,
    val errorNotificationsEnabled: Boolean = false,
)

data class DoiProperties(
    val endpoint: String,
    val uiUrl: String,
    val email: String,
    val user: String,
    val password: String,
    val retry: RetryProperties,
)
