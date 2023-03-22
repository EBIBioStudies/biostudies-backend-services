package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.fire.ZipUtil
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import mu.KotlinLogging
import java.io.File
import java.nio.file.Files

private val logger = KotlinLogging.logger {}

class SubmissionRequestLoader(
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
    private val requestService: SubmissionRequestPersistenceService,
    private val fireTempDirPath: File,
) {
    /**
     * Calculate md5 and size for every file in submission request.
     */
    fun loadRequest(accNo: String, version: Int) {
        val request = requestService.getIndexedRequest(accNo, version)
        val sub = request.submission

        logger.info { "${sub.accNo} ${sub.owner} Started loading submission files" }

        loadSubmissionFiles(accNo, version, sub, request.currentIndex)
        requestService.saveSubmissionRequest(request.withNewStatus(LOADED))

        logger.info { "${sub.accNo} ${sub.owner} Finished loading submission files" }
    }

    private fun loadSubmissionFiles(accNo: String, version: Int, sub: ExtSubmission, startingAt: Int) {
        filesRequestService
            .getSubmissionRequestFiles(accNo, sub.version, startingAt)
            .forEach {
                when (val file = it.file) {
                    is FireFile -> requestService.updateRqtIndex(accNo, version, it.index)
                    is NfsFile -> requestService.updateRqtIndex(it, loadAttributes(sub, file))
                }
                logger.info { "$accNo ${sub.owner} Finished loading file ${it.index}, path='${it.path}'" }
            }
    }

    private fun loadAttributes(sub: ExtSubmission, file: NfsFile): ExtFile {
        return when (file.type) {
            ExtFileType.DIR -> asCompressedFile(sub.accNo, sub.version, file)
            else -> file.copy(md5 = file.file.md5(), size = file.file.size())
        }
    }

    private fun asCompressedFile(accNo: String, version: Int, directory: NfsFile): NfsFile {
        fun compress(file: File): File {
            val tempFolder = fireTempDirPath.resolve("$accNo/$version")
            tempFolder.mkdirs()

            val target = tempFolder.resolve("${file.name}.zip")
            Files.deleteIfExists(target.toPath())
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
            type = ExtFileType.DIR
        )
    }
}
