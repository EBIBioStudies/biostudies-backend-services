package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.submission.domain.postprocessing.ExtPostProcessingService
import ac.uk.ebi.biostd.submission.domain.postprocessing.ExtPostProcessingService.PostprocesMode
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions/extended")
class ExtSubmissionPostProcessResource(
    private val extPostProcessingService: ExtPostProcessingService,
) {
    @PostMapping("/post-process")
    suspend fun postProcessAll() {
        extPostProcessingService.postProcessAll()
    }

    @PostMapping("{accNo}/post-process")
    suspend fun postProcessSubmission(
        @PathVariable accNo: String,
        @RequestParam remote: Boolean = false,
    ) {
        extPostProcessingService.postProcess(accNo, PostprocesMode.ALL, remote)
    }

    @PostMapping("{accNo}/post-process/stats")
    suspend fun calculateSubmissionStats(
        @PathVariable accNo: String,
        @RequestParam remote: Boolean = false,
    ) = extPostProcessingService.postProcess(accNo, PostprocesMode.STATS, remote)

    @PostMapping("{accNo}/post-process/inner-files")
    suspend fun generateInnerSubmissionFiles(
        @PathVariable accNo: String,
        @RequestParam remote: Boolean = false,
    ) {
        extPostProcessingService.postProcess(accNo, PostprocesMode.INNER_FILES, remote)
    }

    @PostMapping("{accNo}/post-process/fallback-pagetab")
    suspend fun generateFallbackSubmissionPageTab(
        @PathVariable accNo: String,
        @RequestParam remote: Boolean = false,
    ) = extPostProcessingService.postProcess(accNo, PostprocesMode.PAGETAB, remote)
}
