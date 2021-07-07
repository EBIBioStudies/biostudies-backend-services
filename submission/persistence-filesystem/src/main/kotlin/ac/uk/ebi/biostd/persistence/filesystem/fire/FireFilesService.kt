package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileProcessingService.processFiles
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import uk.ac.ebi.fire.client.model.FireFile as ClientFireFile

private val logger = KotlinLogging.logger {}

class FireFilesService(
    private val fireWebClient: FireWebClient
) : FilesService {
    override fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode): ExtSubmission {
        logger.info { "Starting processing files of submission ${submission.accNo} over FIRE" }

        fun processFile(file: ExtFile): ExtFile =
            if (file is NfsFile) processNfsFile(file) else processFireFile(file as FireFile)

        val processed = processFiles(submission, ::processFile)

        logger.info { "Finishing processing files of submission ${submission.accNo} over FIRE" }

        return processed
    }

    private fun processFireFile(fireFile: FireFile) :ExtFile {
        TODO("Not yet implemented")
    }

    private fun processNfsFile(nfsFile: NfsFile): FireFile {
        // TODO should this come from the database instead of performing yet another call to the FIRE API?
        val current = fireWebClient.findByPath(nfsFile.fileName)
        return when {
            current == null -> saveFile(nfsFile)
            current.objectMd5 != nfsFile.md5 -> replaceFile(nfsFile, current)
            else -> updateFileMetadata(nfsFile, current)
        }
    }

    private fun saveFile(nfsFile: NfsFile): FireFile {
        val persisted = fireWebClient.save(nfsFile.file, nfsFile.md5)
        fireWebClient.setPath(persisted.fireOid, nfsFile.fileName)

        return updateFileMetadata(nfsFile, persisted)
    }

    private fun replaceFile(new: NfsFile, current: ClientFireFile) =
        saveFile(new).also { fireWebClient.delete(current.fireOid) }

    private fun updateFileMetadata(new: NfsFile, current: ClientFireFile) =
        FireFile(
            new.fileName,
            current.fireOid,
            current.objectMd5,
            current.objectSize.toLong(),
            new.attributes
        )
}
