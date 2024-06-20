package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.fire.ZipUtil
import ebi.ac.uk.coroutines.concurrently
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.io.ext.createTempFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.model.RequestStatus.INDEXED
import ebi.ac.uk.model.RequestStatus.LOADED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import java.io.File

private val logger = KotlinLogging.logger {}

class SubmissionRequestLoader(
    private val concurrency: Int,
    private val fireTempDirPath: File,
    private val eventsPublisherService: EventsPublisherService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
    private val requestService: SubmissionRequestPersistenceService,
) {
    /**
     * Calculate md5 and size for every file in submission request.
     */
    suspend fun loadRequest(
        accNo: String,
        version: Int,
        processId: String,
    ) {
        requestService.onRequest(accNo, version, INDEXED, processId) {
            loadRequest(it.submission)
            RqtUpdate(it.withNewStatus(LOADED))
        }
        eventsPublisherService.requestLoaded(accNo, version)
    }

    private suspend fun loadRequest(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Started loading submission files, concurrency: '$concurrency'" }
        loadSubmissionFiles(sub.accNo, sub)
        logger.info { "${sub.accNo} ${sub.owner} Finished loading submission files, concurrency: '$concurrency'" }
    }

    private suspend fun loadSubmissionFiles(
        accNo: String,
        sub: ExtSubmission,
    ) {
        suspend fun loadFile(rqtFile: SubmissionRequestFile) {
            logger.info { "$accNo ${sub.owner} Started loading file ${rqtFile.index}, path='${rqtFile.path}'" }
            when (val file = rqtFile.file) {
                is FireFile -> {
                    val loaded = rqtFile.copy(status = RequestFileStatus.LOADED)
                    requestService.updateRqtFile(loaded)
                }

                is NfsFile -> {
                    val loaded = rqtFile.copy(file = loadFile(sub, file), status = RequestFileStatus.LOADED)
                    requestService.updateRqtFile(loaded)
                }
            }
            logger.info { "$accNo ${sub.owner} Finished loading file ${rqtFile.index}, path='${rqtFile.path}'" }
        }

        supervisorScope {
            filesRequestService
                .getSubmissionRequestFiles(accNo, sub.version, RequestFileStatus.INDEXED)
                .concurrently(concurrency) { loadFile(it) }
                .collect()
        }
    }

    private suspend fun loadFile(
        sub: ExtSubmission,
        file: NfsFile,
    ): ExtFile =
        withContext(Dispatchers.IO) {
            when {
                file.type == DIR && sub.storageMode == FIRE -> asCompressedFile(sub.accNo, sub.version, file)
                else -> file.copy(md5 = file.file.md5(), size = file.file.size())
            }
        }

    private fun asCompressedFile(
        accNo: String,
        version: Int,
        directory: NfsFile,
    ): NfsFile {
        fun compress(file: File): File {
            val tempFolder = fireTempDirPath.resolve("$accNo/$version")
            tempFolder.mkdirs()

            val target = tempFolder.createTempFile(directory.fileName, ".zip")
            ZipUtil.pack(file, target)

            return target
        }

        val compressed = compress(directory.file)
        return directory.copy(
            filePath = "${directory.filePath}.zip",
            relPath = "${directory.relPath}.zip",
            file = compressed,
            fullPath = compressed.absolutePath,
            md5 = compressed.md5(),
            size = compressed.size(),
            type = DIR,
        )
    }
}
