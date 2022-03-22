package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.request.Md5
import ac.uk.ebi.biostd.persistence.filesystem.service.processFiles
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.allFiles
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import java.io.File
import com.fasterxml.jackson.module.kotlin.*


private val logger = KotlinLogging.logger {}

class FireFilesService(
    private val fireWebClient: FireWebClient,
    private val submissionQueryService: SubmissionQueryService
) : FilesService {
    override fun persistSubmissionFiles(request: FilePersistenceRequest): ExtSubmission {
        var (sub, _, previousFiles) = request
        logger.info { "${sub.accNo} ${sub.owner} Persisting files of submission ${sub.accNo} on FIRE" }

        //cleanSubmissionFolder(sub)
        val mapper = jacksonObjectMapper()

        System.getenv("OUT_OF_BAND_UPLOADS_PATH")?.let {
            val outOfBandUploadsString = File(it).readText(Charsets.UTF_8)
            previousFiles = mapper.readValue(outOfBandUploadsString)
        }

        val config = FireFileProcessingConfig(sub.accNo, sub.owner, sub.relPath, fireWebClient, previousFiles)
        val processed = processFiles(sub) { config.processFile(request.submission, it) }

        logger.info { "${sub.accNo} ${sub.owner} Finished persisting files of submission ${sub.accNo} on FIRE" }

        return processed
    }

    private fun cleanSubmissionFolder(submission: ExtSubmission) {
        submissionQueryService
            .findLatestExtByAccNo(submission.accNo, includeFileListFiles = true)
            ?.allFiles()
            ?.filterIsInstance<FireFile>()
            ?.forEach { fireFile -> cleanFile(fireFile.fireId) }
    }

    // TODO Pivotal ID # 181595553: Separate unsetting path from un-publishing once #180902516 is merged
    private fun cleanFile(fireId: String) {
        fireWebClient.unpublish(fireId)
        fireWebClient.unsetPath(fireId)
    }
}

data class FireFileProcessingConfig(
    val accNo: String,
    val owner: String,
    val relPath: String,
    val fireWebClient: FireWebClient,
    val previousFiles: Map<Md5, ExtFile>
)

fun FireFileProcessingConfig.processFile(sub: ExtSubmission, file: ExtFile): ExtFile =
    if (file is NfsFile) processNfsFile(sub.relPath, file) else file

fun FireFileProcessingConfig.processNfsFile(relPath: String, nfsFile: NfsFile): ExtFile {
    logger.info { "$accNo $owner Persisting file ${nfsFile.fileName} with size ${nfsFile.file.size()} on FIRE" }

    /**
     * Files deleted in previous steps of the incremental submission will be replaced with an empty file
     *  => if file actually needs to be submitted and was pushed out-of-band then md5 will match and db will get updated
     *  otherwise, the FireFile "facade" (without underlying data) will be used, containing all the correct info and identified by path instead of md5sum
     *      and it will get reused to update itself with the values it already has,
     *      in order to make the FileList updates @mongo reflect the state of the fileList file instead of the submission files on disk
     */
    val fileFire = (previousFiles[nfsFile.md5] ?: previousFiles.values.firstOrNull { it.relPath == nfsFile.relPath }) as FireFile?

    return if (fileFire == null) saveFile(relPath, nfsFile) else reusePreviousFile(fileFire, nfsFile)
}

private fun reusePreviousFile(fireFile: FireFile, nfsFile: NfsFile) =
    FireFile(
        nfsFile.filePath,
        nfsFile.relPath,
        fireFile.fireId,
        fireFile.md5,
        fireFile.size,
        nfsFile.attributes
    )

private fun FireFileProcessingConfig.saveFile(subRelPath: String, nfsFile: NfsFile): ExtFile {
    val (filePath, relPath, file, _, _, _, attributes) = nfsFile

    return when {
        nfsFile.file.isDirectory -> FireDirectory(filePath, relPath, file.md5(), file.size(), attributes)
        else -> {
            val store = fireWebClient.save(nfsFile.file, nfsFile.md5)
            fireWebClient.setPath(store.fireOid, "$subRelPath/$relPath")
            FireFile(filePath, relPath, store.fireOid, store.objectMd5, store.objectSize.toLong(), attributes)
        }
    }
}
