package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.submitter.SubmissionMetadataHandler
import ac.uk.ebi.biostd.submission.web.model.FileSubmitWebRequest
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.allFiles
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allSections
import ebi.ac.uk.model.extensions.fileListName

class MetadataUpdateWebHandler(
    private val sourceGenerator: SourceGenerator,
    private val webHandlerHelper: WebHandlerHelper,
    private val serializationService: SerializationService,
    private val extSubmissionService: ExtSubmissionService,
    private val submissionMetadataHandler: SubmissionMetadataHandler
) {
    fun updateMetadata(request: FileSubmitWebRequest): Submission {
        val submission = serializationService.deserializeSubmission(request.submission)
        val previousVersion = extSubmissionService
            .getExtendedSubmission(submission.accNo)
            .apply { webHandlerHelper.requireProcessed(this) }
        val source = sourceGenerator.submissionsList(previousVersion.allFiles.toList())

        setUpFileLists(submission)
        return submissionMetadataHandler.updateMetadata(source, submission, previousVersion).toSimpleSubmission()
    }

    private fun setUpFileLists(submission: Submission) {
        submission.allSections()
            .filter { section -> section.fileListName != null }
            .map { section -> section to section.fileListName!! }
            .forEach { (section, fileListName) -> section.fileList = FileList(fileListName.substringBeforeLast(".")) }
    }
}
