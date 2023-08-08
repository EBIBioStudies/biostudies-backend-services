package uk.ac.ebi.scheduler.migrator.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "app")
@ConstructorBinding
data class ApplicationProperties(
    val concurrency: Int,
    val bioStudies: BioStudies,
)

data class BioStudies(
    val url: String,
    val user: String,
    val password: String,
)
