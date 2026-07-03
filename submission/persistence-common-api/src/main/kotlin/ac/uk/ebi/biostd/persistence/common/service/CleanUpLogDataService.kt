package ac.uk.ebi.biostd.persistence.common.service

import java.time.LocalDateTime

interface CleanUpLogDataService {
    suspend fun logCleanUp(
        email: String,
        jobId: String,
        lastActivity: LocalDateTime,
        userSpacePath: String,
    )

    suspend fun logCleanUpError(
        email: String,
        errorMessage: String,
        userSpacePath: String,
        jobId: String? = null,
    )
}
