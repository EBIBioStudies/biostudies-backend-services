package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ebi.ac.uk.coroutines.waitUntil
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds

class SubmissionTestService(
    private val submissionRepository: SubmissionPersistenceQueryService,
) {
    suspend fun verifyDeleted(accNo: String) {
        waitUntil(
            ofSeconds(60),
            ofMillis(200),
        ) { submissionRepository.existActiveByAccNo(accNo).not() }
    }
}
