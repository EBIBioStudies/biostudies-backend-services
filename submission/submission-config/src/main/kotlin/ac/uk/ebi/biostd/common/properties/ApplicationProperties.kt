package ac.uk.ebi.biostd.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * @param subBasePath subBasePath extra path added as a prefix of submission relative path. Allows to allocate
 * submissions under different paths
 */
@ConfigurationProperties(prefix = "app")
@ConstructorBinding
data class ApplicationProperties(
    val tempDirPath: String,
    val fireTempDirPath: String,
    val submissionPath: String,
    val requestFilesPath: String,
    val ftpPath: String,
    val instanceBaseUrl: String,
    val subBasePath: String?,
    val consumers: Int,
    val maxConsumers: Int,
    val security: SecurityProperties,
    val fire: FireProperties,
    val validator: ValidatorProperties,
    val persistence: PersistenceProperties,
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
    val retry: RetryProperties,
)

data class ValidatorProperties(
    val euToxRiskValidationApi: String,
)

data class PersistenceProperties(
    val enableFire: Boolean = false,
)
