package ac.uk.ebi.biostd.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "app")
@ConstructorBinding
data class ApplicationProperties(
    val tempDirPath: String,
    val fireTempDirPath: String,
    val requestFilesPath: String,
    val submissionPath: String,
    val ftpPath: String,
    val instanceBaseUrl: String,
    val security: SecurityProperties,
    val fire: FireProperties,
    val validator: ValidatorProperties,
    val persistence: PersistenceProperties
)

data class FireProperties(
    val host: String,
    val username: String,
    val password: String
)

data class ValidatorProperties(
    val euToxRiskValidationApi: String
)

class PersistenceProperties(
    val enableFire: Boolean = false
)
