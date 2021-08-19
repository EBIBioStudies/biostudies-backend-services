package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.submission.domain.helpers.RequestSources
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.paths.SubmissionFolderResolver
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.io.File

class ExtSubmissionsWebHandler(
    private val sourceGenerator: SourceGenerator,
    private val submissionService: SubmissionService,
    private val folderResolver: SubmissionFolderResolver,
    private val extSubmissionService: ExtSubmissionService,
    private val extSerializationService: ExtSerializationService
) {
    // TODO test submitting an already existing version
    // TODO refactor common code between web handlers
    fun submit(request: ContentSubmitWebRequest): ExtSubmission {
        val extSubmission = extSerializationService.deserialize<ExtSubmission>(request.submission)
        val previousVersion = submissionService.findPreviousVersion(extSubmission.accNo)
        val source = sourceGenerator.submissionSources(
            RequestSources(
                user = request.submitter,
                files = request.files,
                rootPath = extSubmission.rootPath,
                subFolder = subFolder(previousVersion)
            )
        )
        val fileLists = extSubmission.allFileList.map { source.getFile(it.fileName) }

        return extSubmissionService.submitExtendedSubmission(request.submitter.email, extSubmission, fileLists)
    }

    private fun subFolder(submission: BasicSubmission?): File? =
        submission?.let { folderResolver.getSubFolder(it.relPath).toFile() }
}
