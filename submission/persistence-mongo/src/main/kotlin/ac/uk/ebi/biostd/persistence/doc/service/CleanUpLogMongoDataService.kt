package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.service.CleanUpLogDataService
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.CleanUpErrorMongoRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.CleanUpLogMongoRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocCleanUpError
import ac.uk.ebi.biostd.persistence.doc.model.DocCleanUpLog
import org.bson.types.ObjectId
import java.time.Instant
import java.time.LocalDateTime

class CleanUpLogMongoDataService(
    private val cleanUpLogMongoRepository: CleanUpLogMongoRepository,
    private val cleanUpErrorMongoRepository: CleanUpErrorMongoRepository,
) : CleanUpLogDataService {
    override suspend fun logCleanUp(
        email: String,
        jobId: String,
        lastActivity: LocalDateTime,
        userSpacePath: String,
    ) {
        cleanUpLogMongoRepository.save(
            DocCleanUpLog(
                id = ObjectId(),
                date = Instant.now(),
                jobId = jobId,
                email = email,
                lastActivity = lastActivity,
                userSpacePath = userSpacePath,
            ),
        )
    }

    override suspend fun logCleanUpError(
        email: String,
        errorMessage: String,
        userSpacePath: String,
        jobId: String?,
    ) {
        cleanUpErrorMongoRepository.save(
            DocCleanUpError(
                id = ObjectId(),
                email = email,
                date = Instant.now(),
                errorMessage = errorMessage,
                jobId = jobId,
                userSpacePath = userSpacePath,
            ),
        )
    }
}
