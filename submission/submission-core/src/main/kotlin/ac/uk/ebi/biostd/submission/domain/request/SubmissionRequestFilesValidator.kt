package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.exception.InvalidFileListException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.submission.service.FileSourcesRequest
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ebi.ac.uk.errors.FilesProcessingException
import ebi.ac.uk.errors.InvalidPathException
import ebi.ac.uk.extended.mapping.from.ToExtSectionMapper
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.RequestStatus.FILES_VALIDATED
import ebi.ac.uk.model.RequestStatus.REQUESTED
import ebi.ac.uk.paths.FolderType
import ebi.ac.uk.security.integration.components.SecurityQueryService
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList")
class SubmissionRequestFilesValidator(
    private val requestService: SubmissionRequestPersistenceService,
    private val fileSourcesService: FileSourcesService,
    private val securityService: SecurityQueryService,
    private val toExtSectionMapper: ToExtSectionMapper,
    private val queryService: SubmissionPersistenceQueryService,
    private val pageTabService: PageTabService,
    private val appProperties: ApplicationProperties,
) {
    suspend fun checkFiles(
        accNo: String,
        version: Int,
        processId: String,
    ): SubmissionRequest =
        requestService.onRequest(accNo, version, REQUESTED, processId) {
            when {
                appProperties.asyncMode -> processSafely(it)
                else -> processRequest(it)
            }
        }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun processSafely(request: SubmissionRequest): SubmissionRequest {
        try {
            return processRequest(request)
        } catch (exception: Exception) {
            logger.error(exception) { "Error processing request accNo='${request.accNo}', version=${request.version}" }
            return when (exception) {
                is FilesProcessingException -> request.withErrors(listOf(exception.message))
                is InvalidFileListException -> request.withErrors(listOf(exception.message))
                is InvalidPathException -> request.withErrors(listOf(exception.message))
                else -> request.withErrors(listOf("Unknown processing error. Please contact Admin."))
            }
        }
    }

    private suspend fun processRequest(request: SubmissionRequest): SubmissionRequest {
        val sources = sources(request)
        val sub = request.process!!.submission
        val newSection = toExtSectionMapper.convert(sub.accNo, sub.version, sub.section, sources)
        val withFiles = sub.copy(section = newSection)
        val withPageTab = pageTabService.generatePageTab(withFiles)

        return request.withNewStatus(FILES_VALIDATED, withPageTab)
    }

    private suspend fun sources(submissionRequest: SubmissionRequest): FileSourcesList {
        val request = submissionRequest.process!!
        val submission = request.submission
        val previous = request.previousVersion?.let { queryService.getExtByAccNoAndVersion(submission.accNo, it) }
        var sourceRequest =
            FileSourcesRequest(
                folderType = FolderType.NFS,
                onBehalfUser = submissionRequest.onBehalfUser?.let { securityService.getUser(it) },
                files = submissionRequest.files,
                submitter = securityService.getUser(submission.submitter),
                rootPath = submission.rootPath,
                submission = previous,
                preferredSources = submissionRequest.preferredSources,
            )
        return fileSourcesService.submissionSources(sourceRequest)
    }
}
