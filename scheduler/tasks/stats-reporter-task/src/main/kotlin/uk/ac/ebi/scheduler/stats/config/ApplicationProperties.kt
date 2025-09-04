package uk.ac.ebi.scheduler.stats.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class ApplicationProperties(
    val publishPath: String,
)
