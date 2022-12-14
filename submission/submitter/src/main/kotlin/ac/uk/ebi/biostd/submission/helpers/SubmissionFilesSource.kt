package ac.uk.ebi.biostd.submission.helpers

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ebi.ac.uk.extended.mapping.from.toExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.copyWithAttributes
import ebi.ac.uk.io.sources.DbFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathSource
import ebi.ac.uk.model.Attribute
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.io.File

class SubmissionFilesSource(
    private val accNo: String,
    private val version: Int,
    private val nfsFiles: PathSource,
    private val fireClient: FireClient,
    private val previousVersionFiles: Map<String, ExtFile>,
    private val queryService: SubmissionPersistenceQueryService,
) : FilesSource {
    override val description: String
        get() = "Previous version files"

    override fun getExtFile(path: String, dbFile: DbFile?, attributes: List<Attribute>): ExtFile? {
        return findSubmissionFile(path)?.copyWithAttributes(attributes.map { it.toExtAttribute() })
    }

    override fun getFile(path: String): File? {
        return findSubmissionFile(path)?.let { getFile(it) }
    }

    private fun findSubmissionFile(path: String): ExtFile? {
        return previousVersionFiles[path] ?: queryService.findReferencedFile(accNo, version, path)
    }

    private fun getFile(file: ExtFile): File? {
        return when (file) {
            is NfsFile -> nfsFiles.getFile(file.filePath)
            is FireFile -> fireClient.downloadByFireId(file.fireId, file.fileName)
        }
    }
}
