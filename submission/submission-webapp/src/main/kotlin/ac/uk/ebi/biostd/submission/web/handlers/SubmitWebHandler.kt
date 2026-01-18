package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.files.service.FileServiceFactory
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.biostd.persistence.common.exception.ConcurrentSubException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionRequestDraftService
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionService
import ac.uk.ebi.biostd.submission.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.model.DraftSubmitWebRequest
import ac.uk.ebi.biostd.submission.model.FileSubmitWebRequest
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.model.SubmitWebRequest
import ac.uk.ebi.biostd.submission.model.method
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.FileSourcesRequest
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ebi.ac.uk.base.nullIfBlank
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.ByPassSourceList
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionId
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.withAttributes
import ebi.ac.uk.paths.FolderType

private const val DIRECT_UPLOAD_PATH = "direct-uploads"

@Suppress("CyclomaticComplexMethod", "LongParameterList")
class SubmitWebHandler(
    private val accNoService: AccNoService,
    private val subService: SubmissionService,
    private val extSubService: ExtSubmissionQueryService,
    private val fileSourcesService: FileSourcesService,
    private val serializationService: SerializationService,
    private val toSubmissionMapper: ToSubmissionMapper,
    private val queryService: SubmissionMetaQueryService,
    private val fileServiceFactory: FileServiceFactory,
    private val requestDraftService: SubmissionRequestDraftService,
    private val appProperties: ApplicationProperties,
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

    suspend fun submit(request: DraftSubmitWebRequest): Submission {
        val rqt = buildRequest(request)
        val extSubmission = subService.submit(rqt)
        return toSubmissionMapper.toSimpleSubmission(extSubmission)
    }

    suspend fun submitAsync(request: ContentSubmitWebRequest): SubmissionId {
        val rqt = buildRequest(request)
        subService.submitAsync(rqt)

        return SubmissionId(rqt.accNo, rqt.version)
    }

    suspend fun submitAsync(requests: List<ContentSubmitWebRequest>): List<SubmissionId> {
        val rqt = requests.map { buildRequest(it) }
        subService.submitAsync(rqt)

        return rqt.map { SubmissionId(it.accNo, it.version) }
    }

    suspend fun submitAsync(request: FileSubmitWebRequest): SubmissionId {
        val rqt = buildRequest(request)
        val fileService = fileServiceFactory.forUser(request.config.submitter)
        fileService.uploadFile(DIRECT_UPLOAD_PATH, request.submission)
        subService.submitAsync(rqt)

        return SubmissionId(rqt.accNo, rqt.version)
    }

    suspend fun submitAsync(request: DraftSubmitWebRequest): SubmissionId {
        val rqt = buildRequest(request)
        subService.submitAsync(rqt)

        return SubmissionId(rqt.accNo, rqt.version)
    }

    private suspend fun buildRequest(rqt: SubmitWebRequest): SubmitRequest {
        val (submitter, onBehalfUser, attrs, storageMode, silentMode, singleJobMode) = rqt.config
        val (requestFiles, preferredSources) = rqt.filesConfig

        suspend fun getOrCreateRequest(
            accNo: String?,
            owner: String,
            submission: Submission,
        ): SubmissionRequest {
            suspend fun createDraftFromAccNo(
                accNo: String,
                attachTo: String?,
            ): SubmissionRequest {
                val pageTab = serializationService.serializeSubmission(submission, JSON)
                if (requestDraftService.hasProcessingRequest(accNo)) throw ConcurrentSubException(accNo)
                return requestDraftService.createActiveRequestByAccNo(pageTab, owner, accNo, attachTo)
            }

            suspend fun createNewDraft(): SubmissionRequest {
                val pageTab = serializationService.serializeSubmission(submission, JSON)
                return requestDraftService.createRequestDraft(draft = pageTab, user = owner, submission.attachTo)
            }

            return when {
                rqt is DraftSubmitWebRequest -> requestDraftService.getRequestDraft(rqt.accNo, owner)
                accNo != null -> createDraftFromAccNo(accNo, submission.attachTo)
                else -> createNewDraft()
            }
        }

        /**
         * Deserialize the submission without considering files and retrieve accNo and rootPath.
         */
        suspend fun deserializeSubmission(): Pair<String?, String?> {
            val submission =
                when (rqt) {
                    is ContentSubmitWebRequest -> serializationService.deserializeSubmission(rqt.submission, rqt.format)
                    is FileSubmitWebRequest -> serializationService.deserializeSubmission(rqt.submission)
                    is DraftSubmitWebRequest -> {
                        val draft = requestDraftService.getRequestDraft(rqt.accNo, rqt.owner).draft!!
                        serializationService.deserializeSubmission(draft, JSON)
                    }
                }

            return submission.accNo.nullIfBlank() to submission.rootPath
        }

        /**
         * Deserialize the submission and check the files are present in the list of sources.
         */
        suspend fun deserializeSubmission(source: FileSourcesList): Submission =
            when (rqt) {
                is ContentSubmitWebRequest ->
                    serializationService.deserializeSubmission(rqt.submission, rqt.format, source)

                is FileSubmitWebRequest -> serializationService.deserializeSubmission(rqt.submission, source)

                is DraftSubmitWebRequest -> {
                    val draft = requestDraftService.getRequestDraft(rqt.accNo, rqt.owner).draft!!
                    serializationService.deserializeSubmission(draft, JSON, source)
                }
            }

        /**
         * Create the list of submission sources available based on the given submission.
         */
        fun sourceRequest(
            rootPath: String?,
            previous: ExtSubmission?,
        ): FileSourcesRequest =
            FileSourcesRequest(
                folderType = FolderType.FTP,
                submitter = submitter,
                files = requestFiles,
                onBehalfUser = onBehalfUser,
                rootPath = rootPath,
                submission = previous,
                preferredSources = preferredSources,
            )

        fun getSources(sourceRequest: FileSourcesRequest): FileSourcesList =
            when (appProperties.asyncMode) {
                true -> ByPassSourceList(fileSourcesService.submissionSources(sourceRequest))
                false -> fileSourcesService.submissionSources(sourceRequest)
            }

        /**
         * Process the submission:
         *
         * 1. AccNo, RootPath attributes are extracted from the submission.
         * 2. Submission file sources are obtained.
         * 3. Submission is deserialized including file sources to check both pagetab structure and file presence.
         * 4. Overridden attributes are set.
         * 5. AccNo and version are calculated.
         * 6. Request draft is created if it doesn't exist.
         * 7. Proper submission accNo is set to the draft.
         */
        suspend fun processSubmission(): SubmitRequest {
            val (accNo, rootPath) = deserializeSubmission()
            val previous = accNo?.let { extSubService.findExtendedSubmission(it) }
            val sources = getSources(sourceRequest(rootPath, previous))
            val submission = deserializeSubmission(sources).withAttributes(attrs)
            val collection = submission.attachTo?.let { queryService.getBasicCollection(it) }
            val draft = getOrCreateRequest(accNo, submitter.email, submission)

            return SubmitRequest(
                accNo = draft.accNo,
                version = draft.version,
                relPath = accNoService.getRelPath(draft.accNo),
                submission = submission,
                submitter = submitter,
                owner = submitter.email,
                sources = sources,
                preferredSources = preferredSources,
                requestFiles = requestFiles.orEmpty(),
                method = rqt.method,
                onBehalfUser = onBehalfUser,
                collection = collection,
                previousVersion = previous,
                storageMode = storageMode,
                silentMode = silentMode,
                singleJobMode = singleJobMode,
                newSubmission = draft.newSubmission,
            )
        }

        return processSubmission()
    }
}
