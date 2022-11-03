package ac.uk.ebi.biostd.submission.helpers

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ebi.ac.uk.extended.mapping.from.toExtAttributes
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.allInnerSubmissionFiles
import ebi.ac.uk.io.sources.ConfiguredDbFile
import ebi.ac.uk.io.sources.DbFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.UploadedDbFile
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FILES_RESERVED_ATTRS
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File

class FireFilesSourceFactory(
    private val fireClient: FireClient,
    private val queryService: SubmissionPersistenceQueryService,
) {
    fun createFireSource(): FilesSource = FireFilesSource(fireClient)

    fun createSubmissionFireSource(previousVersion: ExtSubmission): FilesSource {
        val previousVersionFiles = previousVersion
            .allInnerSubmissionFiles
            .groupBy { it.filePath }
            .mapValues { it.value.first() }

        return SubmissionFireFilesSource(
            previousVersion.accNo,
            previousVersion.version,
            fireClient,
            previousVersionFiles,
            queryService,
        )
    }
}

class FireFilesSource(
    private val fireClient: FireClient,
) : FilesSource {
    override fun getExtFile(
        path: String,
        dbFile: DbFile?,
        attributes: List<Attribute>,
    ): ExtFile? {
        return when (dbFile) {
            null -> null
            else -> fireClient.findByDb(dbFile, path, attributes)
        }
    }

    override fun getFile(path: String): File? = null

    override val description: String = "EBI internal files Archive"
}

private class SubmissionFireFilesSource(
    private val accNo: String,
    private val version: Int,
    private val fireClient: FireClient,
    private val previousVersionFiles: Map<String, ExtFile>,
    private val queryService: SubmissionPersistenceQueryService,
) : FilesSource {
    override val description: String = "Submission $accNo files"

    override fun getExtFile(
        path: String,
        dbFile: DbFile?,
        attributes: List<Attribute>,
    ): ExtFile? {
        return findSubmissionFile(path)
    }

    override fun getFile(path: String): File? {
        return findSubmissionFile(path)?.let { fireClient.downloadByFireId((it as FireFile).fireId, it.fileName) }
    }

    private fun findSubmissionFile(path: String): ExtFile? =
        previousVersionFiles[path] ?: queryService.findReferencedFile(accNo, version, path)
}

fun FireApiFile.asFireFile(path: String, attributes: List<Attribute>): FireFile =
    FireFile(
        fireId = fireOid,
        firePath = filesystemEntry?.path,
        filePath = path,
        relPath = "Files/$path",
        md5 = objectMd5,
        size = objectSize,
        type = ExtFileType.FILE,
        attributes = attributes.toExtAttributes(FILES_RESERVED_ATTRS)
    )

private fun FireClient.findByDb(dbFile: DbFile, path: String, attributes: List<Attribute>): FireFile {
    return when (dbFile) {
        is UploadedDbFile -> findByMd5(dbFile.md5).first().asFireFile(path, attributes)
        is ConfiguredDbFile -> asFireFile(path, dbFile, attributes)
    }
}

fun asFireFile(path: String, db: ConfiguredDbFile, attributes: List<Attribute>): FireFile =
    FireFile(
        fireId = db.id,
        firePath = db.path,
        filePath = path,
        relPath = "Files/$path",
        md5 = db.md5,
        type = ExtFileType.FILE,
        size = db.size,
        attributes = attributes.toExtAttributes(FILES_RESERVED_ATTRS)
    )
