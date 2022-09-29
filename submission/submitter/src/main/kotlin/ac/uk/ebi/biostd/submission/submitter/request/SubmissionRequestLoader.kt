package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
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

private val logger = KotlinLogging.logger {}

class SubmissionRequestLoader(
    private val queryService: SubmissionPersistenceQueryService,
    private val persistenceService: SubmissionPersistenceService,
    private val fileProcessingService: FileProcessingService,
    private val pageTabService: PageTabService,
) {
    /**
     * - Calculate md5 and size for every file in submission request.
     * - Calculate the total number of files in the submission
     * - Generate submission pagetab
     */
    fun loadRequest(accNo: String, version: Int): ExtSubmission {
        logger.info { "Started loading pending request accNo='$accNo', version='$version'" }

        val original = queryService.getPendingRequest(accNo, version)
        val owner = original.submission.owner

        logger.info { "Finished loading pending request accNo='$accNo', version='$version'" }

        logger.info { "$accNo $owner Started loading submission files" }

        val (loaded, subFiles) = processRequest(original.submission)
        val withTabFiles = pageTabService.generatePageTab(loaded)
        val totalFiles = subFiles + withTabFiles.allPageTabFiles.size
        val loadedRequest = SubmissionRequest(withTabFiles, original.draftKey, LOADED, totalFiles, currentIndex = 0)
        persistenceService.saveSubmissionRequest(loadedRequest)
        persistenceService.updateRequestTotalFiles(accNo, version, totalFiles)

        logger.info { "$accNo $owner Finished loading submission files" }

        return withTabFiles
    }

    private fun processRequest(sub: ExtSubmission): Pair<ExtSubmission, Int> {
        var totalFiles = 0

        val loadedSubmission = fileProcessingService.processFiles(sub) { file, index ->
            totalFiles++
            processFile(sub, file, index)
        }

        return loadedSubmission to totalFiles
    }

    private fun processFile(sub: ExtSubmission, file: ExtFile, index: Int): ExtFile {
        logger.info { "${sub.accNo} ${sub.owner} Started loading file $index, path='${file.filePath}'" }

        val loadedFile = loadFileAttributes(file)
        persistenceService.updateRequestIndex(sub.accNo, sub.version, index)

        logger.info { "${sub.accNo} ${sub.owner} Finished loading file $index, path='${file.filePath}'" }

        return loadedFile
    }

    private fun loadFileAttributes(file: ExtFile): ExtFile = when (file) {
        is FireFile -> file
        is NfsFile -> file.copy(md5 = file.file.md5(), size = file.file.size())
    }
}
