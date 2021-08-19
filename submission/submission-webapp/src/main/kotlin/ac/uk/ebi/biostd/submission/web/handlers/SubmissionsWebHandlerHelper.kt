package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.submission.domain.helpers.RequestSources
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.web.model.SourcesRequest
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.paths.SubmissionFolderResolver
import java.io.File

class SubmissionsWebHandlerHelper(
    private val sourceGenerator: SourceGenerator,
    private val submissionService: SubmissionService,
    private val folderResolver: SubmissionFolderResolver
) {
    fun submissionSources(request: SourcesRequest): SubmissionSources {
        val previousVersion = submissionService.findPreviousVersion(request.accNo)
        val fileSources = sourceGenerator.submissionSources(
            RequestSources(
                user = request.submitter,
                files = request.files,
                rootPath = request.rootPath,
                subFolder = subFolder(previousVersion)
            )
        )

        return SubmissionSources(fileSources, previousVersion)
    }

    private fun subFolder(submission: BasicSubmission?): File? =
        submission?.let { folderResolver.getSubFolder(it.relPath).toFile() }
}

data class SubmissionSources(
    val fileSources: FilesSource,
    val previousVersion: BasicSubmission?
)
