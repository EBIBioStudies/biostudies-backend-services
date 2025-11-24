package ac.uk.ebi.scheduler.properties

import java.time.Instant

data class SubmissionMigratorProperties(
    val concurrency: Int,
    val delay: Long,
    val await: Long,
    val modifiedBefore: Instant,
    val bioStudies: BioStudies,
)

data class BioStudies(
    val url: String,
    val user: String,
    val password: String,
)
