package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.RELEASED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.REUSED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.UNRELEASED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.submission.exceptions.UnreleasedSubmissionException
import ebi.ac.uk.coroutines.concurrently
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.model.RequestStatus.CHECK_RELEASED
import ebi.ac.uk.model.RequestStatus.FILES_COPIED
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.supervisorScope
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList")
class SubmissionRequestReleaser(
    private val concurrency: Int,
    private val fileStorageService: FileStorageService,
    private val serializationService: ExtSerializationService,
    private val eventsPublisherService: EventsPublisherService,
    private val queryService: SubmissionPersistenceQueryService,
    private val rqtService: SubmissionRequestPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    /**
     * Check the release status of the submission and release it if released flag is true.
     */
    suspend fun checkReleased(
        accNo: String,
        version: Int,
        processId: String,
    ) {
        rqtService.onRequest(accNo, version, FILES_COPIED, processId) {
            if (it.submission.released) {
                releaseRequest(accNo, it)
            } else {
                val current = queryService.findCoreInfo(accNo)
                if (current != null && current.released) unReleaseRequest(accNo, it)
            }

            RqtUpdate(it.withNewStatus(CHECK_RELEASED))
        }

        eventsPublisherService.requestCheckedRelease(accNo, version)
    }

    /**
     * Generates/refresh FTP links for a given submission.
     */
    suspend fun generateFtpLinks(accNo: String) {
        val submission = queryService.getExtByAccNo(accNo, includeFileListFiles = true)
        require(submission.released) { throw UnreleasedSubmissionException() }
        generateFtpLinks(submission)
    }

    private suspend fun releaseRequest(
        accNo: String,
        request: SubmissionRequest,
    ) {
        val sub = request.submission
        logger.info { "$accNo ${sub.owner} Started releasing submission files over ${sub.storageMode}" }
        releaseSubmissionFiles(sub)
        logger.info { "$accNo ${sub.owner} Finished releasing submission files over ${sub.storageMode}" }
    }

    private suspend fun releaseSubmissionFiles(sub: ExtSubmission) {
        suspend fun releaseFile(reqFile: SubmissionRequestFile) {
            when (val file = reqFile.file) {
                is NfsFile -> {
                    val released = reqFile.copy(file = release(sub, reqFile.index, file), status = RELEASED)
                    rqtService.updateRqtFile(released)
                }

                is FireFile -> {
                    val released = if (file.published) file else release(sub, reqFile.index, file)
                    rqtService.updateRqtFile(reqFile.copy(file = released, status = RELEASED))
                }
            }
        }

        supervisorScope {
            filesRequestService
                .getSubmissionRequestFiles(sub.accNo, sub.version, COPIED)
                .concurrently(concurrency) { releaseFile(it) }
                .collect()
        }
    }

    private suspend fun release(
        sub: ExtSubmission,
        idx: Int,
        file: ExtFile,
    ): ExtFile {
        logger.info { "${sub.accNo}, ${sub.owner} Started publishing file $idx - ${file.filePath}" }
        val releasedFile = fileStorageService.releaseSubmissionFile(sub, file)
        logger.info { "${sub.accNo}, ${sub.owner} Finished publishing file $idx - ${file.filePath}" }
        return releasedFile
    }

    private suspend fun unReleaseRequest(
        accNo: String,
        request: SubmissionRequest,
    ) {
        val sub = request.submission
        logger.info { "$accNo ${sub.owner} Started suppressing submission files over ${sub.storageMode}" }
        unReleaseSubmissionFiles(sub)
        logger.info { "$accNo ${sub.owner} Finished suppressing submission files over ${sub.storageMode}" }
    }

    private suspend fun unReleaseSubmissionFiles(sub: ExtSubmission) {
        suspend fun unReleaseFile(reqFile: SubmissionRequestFile) {
            when (val file = reqFile.file) {
                is NfsFile -> {
                    val released = reqFile.copy(file = unRelease(sub, reqFile.index, file), status = UNRELEASED)
                    rqtService.updateRqtFile(released)
                }

                is FireFile -> {
                    val unreleased = reqFile.copy(file = unRelease(sub, reqFile.index, file), status = UNRELEASED)
                    rqtService.updateRqtFile(unreleased)
                }
            }
        }

        supervisorScope {
            filesRequestService
                .getSubmissionRequestFiles(sub.accNo, sub.version, REUSED)
                .concurrently(concurrency) { unReleaseFile(it) }
                .collect()
        }
    }

    private suspend fun unRelease(
        sub: ExtSubmission,
        idx: Int,
        file: ExtFile,
    ): ExtFile {
        logger.info { "${sub.accNo}, ${sub.owner} Started suppressing file $idx - ${file.filePath}" }
        val unreleased = fileStorageService.unReleaseSubmissionFile(sub, file)
        logger.info { "${sub.accNo}, ${sub.owner} Finished suppressing file $idx - ${file.filePath}" }
        return unreleased
    }

    private suspend fun generateFtpLinks(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Started releasing submission files over ${sub.storageMode}" }
        serializationService.filesFlow(sub)
            .filterNot { it is FireFile && it.published }
            .collectIndexed { idx, file -> release(sub, idx, file) }
        logger.info { "${sub.accNo} ${sub.owner} Finished releasing submission files over ${sub.storageMode}" }
    }
}
