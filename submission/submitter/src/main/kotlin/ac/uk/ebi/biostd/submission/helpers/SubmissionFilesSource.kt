package ac.uk.ebi.biostd.submission.helpers

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.io.sources.DbFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathSource
import ebi.ac.uk.model.Attribute
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.io.File

class SubmissionFilesSource(
    private val sub: ExtSubmission,
    private val nfsFiles: PathSource,
    private val fireClient: FireClient,
    private val previousVersionFiles: Map<String, ExtFile>,
    private val queryService: SubmissionPersistenceQueryService,
) : FilesSource {
    override val description: String
        get() = "Files of submission '${sub.accNo}', version ${sub.version}"

    override fun getExtFile(path: String, dbFile: DbFile?, attributes: List<Attribute>): ExtFile? {
        return findSubmissionFile(path)
    }

    override fun getFile(path: String): File? {
        return findSubmissionFile(path)?.let { getFile(it) }
    }

    private fun findSubmissionFile(path: String): ExtFile? {
        return previousVersionFiles[path] ?: queryService.findReferencedFile(sub.accNo, sub.version, path)
    }

    private fun getFile(file: ExtFile): File? {
        return when (sub.storageMode) {
            NFS -> nfsFiles.getFile(file.filePath)
            FIRE -> fireClient.downloadByFireId((file as FireFile).fireId, file.fileName)
        }
    }
}
