package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.files.service.FileServiceFactory
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionService
import ac.uk.ebi.biostd.submission.model.AcceptedSubmission
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
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.withAttributes

private const val DIRECT_UPLOAD_PATH = "direct-uploads"

@Suppress("LongParameterList")
class SubmitWebHandler(
    private val subService: SubmissionService,
    private val extSubService: ExtSubmissionQueryService,
    private val fileSourcesService: FileSourcesService,
    private val serializationService: SerializationService,
    private val toSubmissionMapper: ToSubmissionMapper,
    private val queryService: SubmissionMetaQueryService,
    private val fileServiceFactory: FileServiceFactory,
) {
    suspend fun submit(request: ContentSubmitWebRequest): Submission {
        val rqt = buildRequest(request)
        val extSubmission = subService.submit(rqt)
        return toSubmissionMapper.toSimpleSubmission(extSubmission)
    }

    suspend fun submit(request: FileSubmitWebRequest): Submission {
        val rqt = buildRequest(request)
        val fileService = fileServiceFactory.forUser(request.config.submitter)
        fileService.uploadFile(DIRECT_UPLOAD_PATH, request.submission)
        val extSubmission = subService.submit(rqt)
        return toSubmissionMapper.toSimpleSubmission(extSubmission)
    }

    suspend fun submitAsync(request: ContentSubmitWebRequest): AcceptedSubmission {
        val rqt = buildRequest(request)
        return subService.submitAsync(rqt)
    }

    suspend fun submitAsync(request: FileSubmitWebRequest): AcceptedSubmission {
        val rqt = buildRequest(request)
        val fileService = fileServiceFactory.forUser(request.config.submitter)
        fileService.uploadFile(DIRECT_UPLOAD_PATH, request.submission)
        return subService.submitAsync(rqt)
    }

    private suspend fun buildRequest(rqt: SubmitWebRequest): SubmitRequest {
        val (submitter, onBehalfUser, attrs, storageMode) = rqt.config
        val (files, preferredSources) = rqt.filesConfig

        /**
         * Deserialize the submission without considering files and retrieve accNo and rootPath.
         */
        fun deserializeSubmission(): Pair<String, String?> {
            val submission = when (rqt) {
                is ContentSubmitWebRequest -> serializationService.deserializeSubmission(rqt.submission, rqt.format)
                is FileSubmitWebRequest -> serializationService.deserializeSubmission(rqt.submission)
            }
            return submission.accNo to submission.rootPath
        }

        /**
         * Deserialize the submission and check file presence in the list of sources.
         */
        suspend fun deserializeSubmission(source: FileSourcesList): Submission = when (rqt) {
            is ContentSubmitWebRequest -> serializationService.deserializeSubmission(rqt.submission, rqt.format, source)
            is FileSubmitWebRequest -> serializationService.deserializeSubmission(rqt.submission, source)
        }

        /**
         * Create the list of submission sources available based on the given submission.
         */
        fun sourceRequest(rootPath: String?, previous: ExtSubmission?): FileSourcesRequest = FileSourcesRequest(
            submitter = submitter,
            files = files,
            onBehalfUser = onBehalfUser,
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
        suspend fun processSubmission(): SubmitRequest {
            val (accNo, rootPath) = deserializeSubmission()
            val previous = extSubService.findExtendedSubmission(accNo)
            val sources = fileSourcesService.submissionSources(sourceRequest(rootPath, previous))
            val submission = deserializeSubmission(sources).withAttributes(attrs)
            val collection = submission.attachTo?.let { queryService.getBasicCollection(it) }

            return SubmitRequest(
                submission = submission,
                submitter = submitter,
                sources = sources,
                method = rqt.method,
                onBehalfUser = onBehalfUser,
                draftKey = rqt.draftKey,
                collection = collection,
                previousVersion = previous,
                storageMode = storageMode,
            )
        }

        return processSubmission()
    }
}
