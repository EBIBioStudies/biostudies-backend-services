package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.submission.domain.helpers.OnBehalfUtils
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.exceptions.ConcurrentSubException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.service.FileSourcesRequest
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.FileSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.SubmitWebRequest
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod.FILE
import ebi.ac.uk.model.extensions.rootPath

private const val DIRECT_UPLOAD_PATH = "direct-uploads"

@Suppress("LongParameterList")
class SubmitWebHandler(
    private val submissionService: SubmissionService,
    private val extSubmissionService: ExtSubmissionQueryService,
    private val fileSourcesService: FileSourcesService,
    private val serializationService: SerializationService,
    private val userFilesService: UserFilesService,
    private val toSubmissionMapper: ToSubmissionMapper,
    private val onBehalfUtils: OnBehalfUtils,
) {
    fun submit(request: ContentSubmitWebRequest): Submission {
        val rqt = buildRequest(request)
        val extSubmission = submissionService.submit(rqt)
        return toSubmissionMapper.toSimpleSubmission(extSubmission)
    }

    fun submit(request: FileSubmitWebRequest): Submission {
        val rqt = buildRequest(request)
        userFilesService.uploadFile(request.config.submitter, DIRECT_UPLOAD_PATH, request.submission)
        val extSubmission = submissionService.submit(rqt)
        return toSubmissionMapper.toSimpleSubmission(extSubmission)
    }

    fun submitAsync(request: ContentSubmitWebRequest) {
        val rqt = buildRequest(request)
        submissionService.submitAsync(rqt)
    }

    fun submitAsync(request: FileSubmitWebRequest) {
        val rqt = buildRequest(request)
        userFilesService.uploadFile(request.config.submitter, DIRECT_UPLOAD_PATH, request.submission)
        submissionService.submitAsync(rqt)
    }

    private fun buildRequest(request: SubmitWebRequest): SubmitRequest {
        val (submitter, attrs) = request.config
        val (files, preferredSources) = request.filesConfig

        /**
         * Return the draft key of the submit request.
         */
        fun draftKey(rqt: SubmitWebRequest) = when (rqt) {
            is ContentSubmitWebRequest -> rqt.draftKey
            is FileSubmitWebRequest -> null
        }

        /**
         * Deserialize the submission without considering files.
         */
        fun submission(rqt: SubmitWebRequest): Submission = when (rqt) {
            is ContentSubmitWebRequest -> serializationService.deserializeSubmission(rqt.submission, rqt.format)
            is FileSubmitWebRequest -> serializationService.deserializeSubmission(rqt.submission)
        }

        /**
         * Deserialize the submission and check file presence in the list of sources.
         */
        fun submission(rqt: SubmitWebRequest, source: FileSourcesList): Submission = when (rqt) {
            is ContentSubmitWebRequest -> serializationService.deserializeSubmission(rqt.submission, rqt.format, source)
            is FileSubmitWebRequest -> serializationService.deserializeSubmission(rqt.submission, source)
        }

        /**
         * Create the list of submission sources available based on the given submission.
         */
        fun sourceRequest(sub: Submission, previous: ExtSubmission?): FileSourcesRequest = FileSourcesRequest(
            submitter = submitter,
            files = files,
            onBehalfUser = request.onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) },
            rootPath = sub.rootPath,
            submission = previous,
            preferredSources = preferredSources
        )

        /**
         * Overrides the given list of attributes in the current submission.
         */
        fun withAttributes(submission: Submission, attrs: List<ExtAttributeDetail>): Submission {
            attrs.forEach { submission[it.name] = it.value }
            return submission
        }

        /**
         * Ensure there is not pending for processing  submission for the given request.
         */
        fun requireNotProcessing(sub: Submission) =
            require(extSubmissionService.hasPendingRequest(sub.accNo).not()) { throw ConcurrentSubException(sub.accNo) }

        val sub = submission(request).also { requireNotProcessing(it) }
        val previous = extSubmissionService.findExtendedSubmission(sub.accNo)
        val source = fileSourcesService.submissionSources(sourceRequest(sub, previous))
        return SubmitRequest(
            submission = withAttributes(submission(request, source), attrs),
            submitter = submitter,
            onBehalfUser = request.onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) },
            sources = source,
            method = FILE,
            draftKey = draftKey(request)
        )
    }
}
