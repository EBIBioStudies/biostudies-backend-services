package uk.ac.ebi.scheduler.stats.api

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "stats")
data class StatsReporterProperties(
    val publishPath: String,
    val persistence: Persistence,
)

data class Persistence(
    val uri: String,
    val database: String,
)
