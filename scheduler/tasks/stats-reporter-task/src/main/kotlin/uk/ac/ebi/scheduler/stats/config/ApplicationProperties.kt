package uk.ac.ebi.scheduler.stats.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "app")
@ConstructorBinding
data class ApplicationProperties(
    val outputPath: String,
    val publishPath: String,
    val ssh: SshConfiguration,
)

data class SshConfiguration(
    val user: String,
    val key: String,
    val server: String,
)
