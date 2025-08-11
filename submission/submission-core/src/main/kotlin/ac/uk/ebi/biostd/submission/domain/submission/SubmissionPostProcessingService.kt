package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.submission.stats.SubmissionStatsService
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionPostProcessingService(
    private val submissionStatsService: SubmissionStatsService,
) {
    suspend fun postProcess(accNo: String) {
        submissionStatsService.calculateStats(accNo)
    }
}
