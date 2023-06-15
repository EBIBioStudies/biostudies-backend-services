package uk.ac.ebi.io.sources

import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ebi.ac.uk.extended.mapping.from.toExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.copyWithAttributes
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Attribute
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

    override fun getExtFile(path: String, type: String, attributes: List<Attribute>): ExtFile? {
        return findSubmissionFile(path)?.copyWithAttributes(attributes.map { it.toExtAttribute() })
    }

    override fun getFileList(path: String): File? {
        return findSubmissionFile(path)?.let { getFile(it) }
    }

    private fun findSubmissionFile(path: String): ExtFile? {
        return previousVersionFiles[path] ?: filesRepository.findReferencedFile(sub, path)
    }

    private fun getFile(file: ExtFile): File? {
        return when (file) {
            is NfsFile -> nfsFiles.getFileList(file.filePath)
            is FireFile -> fireClient.downloadByPath(file.firePath!!)
        }
    }
}
