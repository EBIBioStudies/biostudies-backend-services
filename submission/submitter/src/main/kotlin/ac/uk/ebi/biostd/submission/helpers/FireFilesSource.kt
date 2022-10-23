package ac.uk.ebi.biostd.submission.helpers

import ebi.ac.uk.extended.mapping.from.toExtAttributes
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.io.sources.ConfiguredFileDb
import ebi.ac.uk.io.sources.FileDb
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.UploadedFileDb
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FILES_RESERVED_ATTRS
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File
import java.nio.file.Path

class FireFilesSourceFactory(
    private val fireClient: FireClient,
) {
    fun createFireSource(): FilesSource = FireFilesSource(fireClient)
    fun createSubmissionFireSource(accNo: String, subPath: Path): FilesSource =
        SubmissionFireFilesSource(accNo, fireClient, subPath)
}

class FireFilesSource(
    private val fireClient: FireClient,
) : FilesSource {
    override fun getExtFile(
        path: String,
        fileDb: FileDb?,
        attributes: List<Attribute>,
    ): ExtFile? {
        return when (fileDb) {
            null -> null
            else -> fireClient.findByDb(fileDb, path, attributes)
        }
    }

    override fun getFile(path: String, fileDb: FileDb?): File? =
        if (fileDb == null) null else fireClient.downloadByMd5(fileDb.md5)

    override val description: String = "EBI internal files Archive"
}

private class SubmissionFireFilesSource(
    accNo: String,
    private val fireClient: FireClient,
    private val subPath: Path,
) : FilesSource {
    override val description: String = "Submission $accNo files"

    override fun getExtFile(
        path: String,
        fileDb: FileDb?,
        attributes: List<Attribute>,
    ): ExtFile? {
        return when (fileDb) {
            null -> fireClient.findByPath(subPath.resolve(path).toString())?.asFireFile(path, attributes)
            else -> fireClient.findByDb(fileDb, path, attributes)
        }
    }

    override fun getFile(path: String, fileDb: FileDb?): File? =
        if (fileDb == null) fireClient.downloadByPath(subPath.resolve(path).toString())
        else fireClient.downloadByMd5(fileDb.md5)
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

private fun FireClient.findByDb(fileDb: FileDb, path: String, attributes: List<Attribute>): FireFile {
    return when (fileDb) {
        is UploadedFileDb -> findByMd5(fileDb.md5).first().asFireFile(path, attributes)
        is ConfiguredFileDb -> asFireFile(path, fileDb, attributes)
    }
}

fun asFireFile(path: String, db: ConfiguredFileDb, attributes: List<Attribute>): FireFile =
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
