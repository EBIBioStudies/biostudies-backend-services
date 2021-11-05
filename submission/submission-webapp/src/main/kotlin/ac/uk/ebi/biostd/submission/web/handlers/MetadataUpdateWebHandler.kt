package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.web.model.FileSubmitWebRequest
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.allFiles
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod

class MetadataUpdateWebHandler(
    private val sourceGenerator: SourceGenerator,
    private val webHandlerHelper: WebHandlerHelper,
    private val submissionService: SubmissionService,
    private val serializationService: SerializationService,
    private val extSubmissionService: ExtSubmissionService
) {
    fun updateMetadata(request: FileSubmitWebRequest): Submission {
        val sub = serializationService.deserializeSubmission(request.submission)

        val extSub = extSubmissionService
            .getExtendedSubmission(sub.accNo)
            .also { webHandlerHelper.requireProcessed(it) }

        val source = sourceGenerator.submissionsList(extSub.allFiles)
        val submitRequest = SubmissionRequest(
            sub,
            request.submitter,
            source,
            SubmissionMethod.valueOf(extSub.method.name),
            FileMode.MOVE,
            request.onBehalfRequest?.let { webHandlerHelper.getOnBehalfUser(it) },
            sub.accNo
        )

        return submissionService.submit(submitRequest).toSimpleSubmission()
    }
}
