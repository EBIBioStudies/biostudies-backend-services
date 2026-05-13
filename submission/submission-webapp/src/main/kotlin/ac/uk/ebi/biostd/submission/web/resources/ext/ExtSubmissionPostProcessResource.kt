package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.submission.domain.postprocessing.ExtPostProcessingService
import ac.uk.ebi.biostd.submission.domain.postprocessing.ExtPostProcessingService.PostProcessMode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions/extended")
@Tag(name = "Post Processing", description = "Run post-processing tasks that enrich extended submissions after ingest.")
class ExtSubmissionPostProcessResource(
    private val extPostProcessingService: ExtPostProcessingService,
) {
    @PostMapping("/post-process")
    @Operation(
        summary = "Post-process All Submissions",
        description = "Run all configured post-processing tasks for all eligible extended submissions.",
    )
    suspend fun postProcessAll() {
        extPostProcessingService.postProcessAll()
    }

    @PostMapping("/{accNo}/post-process")
    @Operation(
        summary = "Post-process Submission",
        description = "Run all post-processing tasks for one submission accession.",
    )
    suspend fun postProcessSubmission(
        @PathVariable accNo: String,
        @RequestParam remote: Boolean = false,
    ) {
        extPostProcessingService.postProcess(accNo, PostProcessMode.ALL, remote)
    }

    @PostMapping("/{accNo}/post-process/stats")
    @Operation(
        summary = "Calculate Submission Statistics",
        description = "Calculate derived statistics for one extended submission.",
    )
    suspend fun calculateSubmissionStats(
        @PathVariable accNo: String,
        @RequestParam remote: Boolean = false,
    ) = extPostProcessingService.postProcess(accNo, PostProcessMode.STATS, remote)

    @PostMapping("/{accNo}/post-process/inner-files")
    @Operation(
        summary = "Generate Inner Files",
        description = "Generate inner-file metadata for one extended submission.",
    )
    suspend fun generateInnerSubmissionFiles(
        @PathVariable accNo: String,
        @RequestParam remote: Boolean = false,
    ) {
        extPostProcessingService.postProcess(accNo, PostProcessMode.INNER_FILES, remote)
    }

    @PostMapping("/{accNo}/post-process/fallback-pagetab")
    @Operation(
        summary = "Generate Fallback PageTab",
        description = "Generate fallback PageTab content for one extended submission.",
    )
    suspend fun generateFallbackSubmissionPageTab(
        @PathVariable accNo: String,
        @RequestParam remote: Boolean = false,
    ) = extPostProcessingService.postProcess(accNo, PostProcessMode.PAGETAB, remote)

    @PostMapping("/{accNo}/post-process/doi")
    @Operation(
        summary = "Post-process DOI",
        description = "Run DOI-related post-processing for one extended submission.",
    )
    suspend fun generateDoi(
        @PathVariable accNo: String,
        @RequestParam remote: Boolean = false,
    ) = extPostProcessingService.postProcess(accNo, PostProcessMode.DOI, remote)
}
