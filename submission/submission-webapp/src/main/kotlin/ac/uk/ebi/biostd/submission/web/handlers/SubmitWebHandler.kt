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
import ac.uk.ebi.biostd.submission.web.model.draftKey
import ac.uk.ebi.biostd.submission.web.model.method
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.withAttributes

private const val DIRECT_UPLOAD_PATH = "direct-uploads"

@Suppress("LongParameterList")
class SubmitWebHandler(
    private val subService: SubmissionService,
    private val extSubService: ExtSubmissionQueryService,
    private val fileSourcesService: FileSourcesService,
    private val serializationService: SerializationService,
    private val userFilesService: UserFilesService,
    private val toSubmissionMapper: ToSubmissionMapper,
    private val onBehalfUtils: OnBehalfUtils,
) {
    fun submit(request: ContentSubmitWebRequest): Submission {
        val rqt = buildRequest(request)
        val extSubmission = subService.submit(rqt)
        return toSubmissionMapper.toSimpleSubmission(extSubmission)
    }

    fun submit(request: FileSubmitWebRequest): Submission {
        val rqt = buildRequest(request)
        userFilesService.uploadFile(request.config.submitter, DIRECT_UPLOAD_PATH, request.submission)
        val extSubmission = subService.submit(rqt)
        return toSubmissionMapper.toSimpleSubmission(extSubmission)
    }

    fun submitAsync(request: ContentSubmitWebRequest) {
        val rqt = buildRequest(request)
        subService.submitAsync(rqt)
    }

    fun submitAsync(request: FileSubmitWebRequest) {
        val rqt = buildRequest(request)
        userFilesService.uploadFile(request.config.submitter, DIRECT_UPLOAD_PATH, request.submission)
        subService.submitAsync(rqt)
    }

    private fun buildRequest(rqt: SubmitWebRequest): SubmitRequest {
        val (submitter, attrs) = rqt.config
        val (files, preferredSources) = rqt.filesConfig
        val onBehalfUser = rqt.onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) }

        /**
         * Deserialize the submission without considering files.
         */
        fun submissionAttributes(): Pair<String, String?> {
            val submission = when (rqt) {
                is ContentSubmitWebRequest -> serializationService.deserializeSubmission(rqt.submission, rqt.format)
                is FileSubmitWebRequest -> serializationService.deserializeSubmission(rqt.submission)
            }
            return submission.accNo to submission.rootPath
        }

        /**
         * Deserialize the submission and check file presence in the list of sources.
         */
        fun deserializeSubmission(source: FileSourcesList): Submission = when (rqt) {
            is ContentSubmitWebRequest -> serializationService.deserializeSubmission(rqt.submission, rqt.format, source)
            is FileSubmitWebRequest -> serializationService.deserializeSubmission(rqt.submission, source)
        }

        /**
         * Create the list of submission sources available based on the given submission.
         */
        fun sourceRequest(rootPath: String?, previous: ExtSubmission?): FileSourcesRequest = FileSourcesRequest(
            submitter = submitter,
            files = files,
            onBehalfUser = rqt.onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) },
            rootPath = rootPath,
            submission = previous,
            preferredSources = preferredSources
        )

        /**
         * Process the given submission:
         *
         * 1. AccNo, RootPath attributes are extracted from Submission.
         * 2. Submission file sources are obtained.
         * 3. Submission is deserialized including file sources to check both pagetab structure and file presence.
         * 4. Overridden attributes are set.
         */
        fun processSubmission(): Pair<Submission, FileSourcesList> {
            val (accNo, rootPath) = submissionAttributes()
            require(extSubService.hasPendingRequest(accNo).not()) { throw ConcurrentSubException(accNo) }

            val previous = extSubService.findExtendedSubmission(accNo)
            val source = fileSourcesService.submissionSources(sourceRequest(rootPath, previous))
            val submission = deserializeSubmission(source)
            return submission.withAttributes(attrs) to source
        }

        val (sub, sources) = processSubmission()
        return SubmitRequest(sub, submitter, sources, rqt.method, onBehalfUser, rqt.draftKey)
    }
}
