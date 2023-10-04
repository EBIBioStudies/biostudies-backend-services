package uk.ac.ebi.scheduler.stats.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "stats")
@ConstructorBinding
data class StatsReporterProperties(
    val outputPath: String,
    val publishPath: String,
    val persistence: Persistence,
)

data class Persistence(
    val uri: String,
    val database: String,
)
