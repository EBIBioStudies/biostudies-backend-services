package uk.ac.ebi.io.sources

import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import uk.ac.ebi.biostd.base.ensureSuffix
import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtFile
import uk.ac.ebi.biostd.extended.model.ExtSubmission
import uk.ac.ebi.biostd.extended.model.FileSourceType.SUBMISSION
import uk.ac.ebi.biostd.extended.model.FireFile
import uk.ac.ebi.biostd.extended.model.NfsFile
import uk.ac.ebi.biostd.extended.model.RequestFile
import uk.ac.ebi.biostd.extended.model.StorageMode
import uk.ac.ebi.biostd.extended.model.typeSafeCopy
import uk.ac.ebi.biostd.fire.client.integration.web.FireClient
import uk.ac.ebi.biostd.io.sources.FilesSource
import uk.ac.ebi.biostd.model.constants.FileFields
import java.io.File

internal class SubmissionFilesSource(
    private val sub: ExtSubmission,
    private val fileSource: FilesSource,
    private val fireClient: FireClient,
    private val previousVersionFiles: Map<String, ExtFile>,
    private val filesRepository: SubmissionFilesPersistenceService,
) : FilesSource {
    override val description: String
        get() = fileSource.description

    override suspend fun getExtFile(
        path: String,
        type: String,
        attributes: List<ExtAttribute>,
    ): ExtFile? {
        val filePath = expectedPath(path, type)
        return findSubmissionFile(filePath)?.typeSafeCopy(attributes, sourceType = SUBMISSION)
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
            is NfsFile -> fileSource.getFileList(file.filePath)
            is FireFile -> fireClient.downloadByPath(file.firePath)
            is RequestFile -> error("Can not obtain File instance from RequestFile ${file.filePath}")
        }
}
