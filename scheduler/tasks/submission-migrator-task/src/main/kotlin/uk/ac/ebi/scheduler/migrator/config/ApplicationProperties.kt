package uk.ac.ebi.scheduler.migrator.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class ApplicationProperties(
    val concurrency: Int,
    val delay: Long,
    val await: Long,
    val accNoPattern: String,
    val bioStudies: BioStudies,
)

data class BioStudies(
    val url: String,
    val user: String,
    val password: String,
)
