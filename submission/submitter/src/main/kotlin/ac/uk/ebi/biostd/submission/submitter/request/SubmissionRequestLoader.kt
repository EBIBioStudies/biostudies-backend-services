package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.allPageTabFiles
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

class SubmissionRequestLoader(
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
    private val requestService: SubmissionRequestPersistenceService,
    private val fileProcessingService: FileProcessingService,
    private val pageTabService: PageTabService,
) {
    /**
     * - Calculate md5 and size for every file in submission request.
     * - Generate submission pagetab
     */
    fun loadRequest(accNo: String, version: Int): ExtSubmission {
        val request = requestService.getIndexedRequest(accNo, version)
        val sub = request.submission

        logger.info { "${sub.accNo} ${sub.owner} Started loading submission files" }

        loadSubmissionFiles(sub, request.currentIndex)
        val loaded = loadSubmission(sub)
        val withTabFiles = pageTabService.generatePageTab(loaded)
        loadPagetabFiles(withTabFiles, request.totalFiles)

        val totalFiles = request.totalFiles + withTabFiles.allPageTabFiles.size
        val loadedRequest = request.copy(
            submission = withTabFiles,
            status = LOADED,
            currentIndex = 0,
            modificationTime = OffsetDateTime.now(),
        )

        requestService.saveSubmissionRequest(loadedRequest)
        requestService.updateRequestTotalFiles(accNo, version, totalFiles)

        logger.info { "${sub.accNo} ${sub.owner} Finished loading submission files" }

        return withTabFiles
    }

    private fun loadSubmissionFiles(sub: ExtSubmission, startingAt: Int) {
        fun loadSubmissionFile(file: ExtFile, idx: Int) {
            logger.info { "${sub.accNo} ${sub.owner} Started loading file $idx, path='${file.filePath}'" }
            val loadedFile = SubmissionRequestFile(sub.accNo, sub.version, idx, file.filePath, loadFileAttributes(file))
            filesRequestService.upsertSubmissionRequestFile(loadedFile)
            requestService.updateRequestIndex(sub.accNo, sub.version, idx)
            logger.info { "${sub.accNo} ${sub.owner} Finished loading file $idx, path='${file.filePath}'" }
        }

        filesRequestService
            .getSubmissionRequestFiles(sub.accNo, sub.version, sub.relPath, startingAt)
            .forEach { loadSubmissionFile(it.file, it.index) }
    }

    /**
     * TODO Once the pagetab is generated from the request files, this step won't be necessary.
     * See https://www.pivotaltracker.com/story/show/183557519
     */
    private fun loadSubmission(sub: ExtSubmission) =
        fileProcessingService.processFiles(sub) { file, _ ->
            filesRequestService.getSubmissionRequestFile(sub.accNo, sub.version, sub.relPath, file.filePath).file
        }

    private fun loadPagetabFiles(sub: ExtSubmission, totalFiles: Int) {
        fun loadPagetabFile(file: ExtFile, index: Int) {
            val pagetabFile = SubmissionRequestFile(sub.accNo, sub.version, index, file.filePath, file)
            filesRequestService.upsertSubmissionRequestFile(pagetabFile)
        }

        sub.allPageTabFiles.forEachIndexed { index, file -> loadPagetabFile(file, totalFiles + index + 1) }
    }

    private fun loadFileAttributes(file: ExtFile): ExtFile = when (file) {
        is FireFile -> file
        is NfsFile -> file.copy(md5 = file.file.md5(), size = file.file.size())
    }
}
