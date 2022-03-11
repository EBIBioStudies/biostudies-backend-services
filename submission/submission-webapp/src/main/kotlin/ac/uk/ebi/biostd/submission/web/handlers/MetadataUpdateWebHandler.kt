package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.web.model.FileSubmitWebRequest
import ebi.ac.uk.extended.mapping.to.toFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.MOVE
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.extended.model.allFiles
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.extensions.allSections
import ebi.ac.uk.model.extensions.fileListName

class MetadataUpdateWebHandler(
    private val sourceGenerator: SourceGenerator,
    private val webHandlerHelper: WebHandlerHelper,
    private val submissionService: SubmissionService,
    private val serializationService: SerializationService,
    private val extSubmissionService: ExtSubmissionService
) {
    fun updateMetadata(request: FileSubmitWebRequest) {
        val submission = serializationService.deserializeSubmission(request.submission)
        val previousVersion = extSubmissionService
            .getExtendedSubmission(submission.accNo, includeFileListFiles = true)
            .apply { webHandlerHelper.requireProcessed(this) }

        setUpFileLists(submission, previousVersion.allFileList)

        val sources = sourceGenerator.submissionsList(previousVersion.allFiles().toList())
        val submitRequest = SubmitRequest(
            submission,
            request.submitter,
            sources,
            SubmissionMethod.valueOf(previousVersion.method.name),
            MOVE,
            request.onBehalfRequest?.let { webHandlerHelper.getOnBehalfUser(it) }
        )

        submissionService.submitAsync(submitRequest)
    }

    private fun setUpFileLists(submission: Submission, previousFileLists: List<ExtFileList>) {
        submission.allSections()
            .filter { section -> section.fileListName != null }
            .map { section -> section to section.fileListName!! }
            .forEach { (section, fileListName) -> section.fileList = buildFileList(fileListName, previousFileLists) }
    }

    private fun buildFileList(fileListName: String, previousFileLists: List<ExtFileList>): FileList {
        val fileName = fileListName.substringBeforeLast(".")
        val referencedFiles = previousFileLists.first { it.fileName == fileName }.files.map { it.toFile() }

        return FileList(fileName, referencedFiles)
    }
}
