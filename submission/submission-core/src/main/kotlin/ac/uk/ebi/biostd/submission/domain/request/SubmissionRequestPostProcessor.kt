package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.domain.postprocessing.LocalPostProcessingService
import ebi.ac.uk.model.RequestStatus.POST_PROCESSED
import ebi.ac.uk.model.RequestStatus.PROCESSED

class SubmissionRequestPostProcessor(
    private val requestService: SubmissionRequestPersistenceService,
    private val postProcessorService: LocalPostProcessingService,
) {
    suspend fun postProcess(
        accNo: String,
        version: Int,
        processId: String,
    ): SubmissionRequest =
        requestService.onRequest(accNo, version, PROCESSED, processId) {
            postProcessorService.postProcess(it.accNo, registerDoi = true)
            it.withNewStatus(POST_PROCESSED)
        }
}
