package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.common.properties.Mode
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionPostProcessingService
import ac.uk.ebi.biostd.submission.domain.submitter.RemoteSubmitterExecutor
import ebi.ac.uk.extended.model.ExtFile
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions/extended")
class ExtSubmissionPostProcessResource(
    private val remoteSubmitterExecutor: RemoteSubmitterExecutor,
    private val submissionPostProcessingService: SubmissionPostProcessingService,
) {
    @PostMapping("/post-process")
    suspend fun postProcessAll() {
        remoteSubmitterExecutor.executeRemotely(emptyList(), Mode.POST_PROCESS_ALL)
    }

    @PostMapping("{accNo}/post-process")
    suspend fun postProcessSubmission(
        @PathVariable accNo: String,
    ) {
        submissionPostProcessingService.postProcess(accNo)
    }

    @PostMapping("{accNo}/post-process/stats")
    suspend fun calculateSubmissionStats(
        @PathVariable accNo: String,
    ): List<SubmissionStat> = submissionPostProcessingService.calculateStats(accNo)

    @PostMapping("{accNo}/post-process/fallback-pagetab")
    suspend fun generateFallbackSubmissionPageTab(
        @PathVariable accNo: String,
    ): List<ExtFile> = submissionPostProcessingService.generateFallbackPageTabFiles(accNo)
}
