package ac.uk.ebi.biostd.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "app.task")
@ConstructorBinding
data class TaskHostProperties(
    val enableTaskMode: Boolean,
    val jarLocation: String,
    val logsLocation: String,
    val configFilePath: String,
    val cluster: ClusterProperties,
)

data class ClusterProperties(
    val user: String,
    val key: String,
    val server: String,
    val logsPath: String,
)
