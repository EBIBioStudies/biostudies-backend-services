package uk.ac.ebi.io.sources

import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ebi.ac.uk.base.ensureSuffix
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.RequestFile
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.copyWithAttributes
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.constants.FileFields
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.io.File

internal class SubmissionFilesSource(
    private val sub: ExtSubmission,
    private val nfsFiles: PathSource,
    private val fireClient: FireClient,
    private val previousVersionFiles: Map<String, ExtFile>,
    private val filesRepository: SubmissionFilesPersistenceService,
) : FilesSource {
    override val description: String
        get() = "Previous version files"

    override suspend fun getExtFile(
        path: String,
        type: String,
        attributes: List<ExtAttribute>,
    ): ExtFile? {
        val filePath = expectedPath(path, type)
        return findSubmissionFile(filePath)?.copyWithAttributes(attributes)
    }

    private fun expectedPath(
        path: String,
        type: String,
    ): String =
        when (sub.storageMode) {
            StorageMode.FIRE -> if (type == FileFields.DIRECTORY_TYPE.value) path.ensureSuffix(".zip") else path
            else -> path
        }

    override suspend fun getFileList(path: String): File? = findSubmissionFile(path)?.let { getFile(it) }

    private suspend fun findSubmissionFile(path: String): ExtFile? =
        previousVersionFiles[path] ?: filesRepository.findReferencedFile(sub, path)

    private suspend fun getFile(file: ExtFile): File? =
        when (file) {
            is NfsFile -> nfsFiles.getFileList(file.filePath)
            is FireFile -> fireClient.downloadByPath(file.firePath)
            is RequestFile -> error("Can not obtain File instance from RequestFile ${file.filePath}")
        }
}
