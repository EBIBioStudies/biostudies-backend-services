package ac.uk.ebi.biostd.submission.domain.helpers

import ebi.ac.uk.extended.mapping.from.toExtAttributes
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.io.sources.FilesSource
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
        md5: String?,
        attributes: List<Attribute>,
    ): ExtFile? {
        return when (md5) {
            null -> null
            else -> fireClient.findByMd5(md5).firstOrNull()?.asFireFile(path, attributes)
        }
    }

    override fun getFile(path: String, md5: String?): File? = if (md5 == null) null else fireClient.downloadByMd5(md5)
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
        md5: String?,
        attributes: List<Attribute>,
    ): ExtFile? {
        return when (md5) {
            null -> fireClient.findByPath(subPath.resolve(path).toString())?.asFireFile(path, attributes)
            else -> fireClient.findByMd5(md5).firstOrNull()?.asFireFile(path, attributes)
        }
    }

    override fun getFile(path: String, md5: String?): File? =
        if (md5 == null) fireClient.downloadByPath(subPath.resolve(path).toString())
        else fireClient.downloadByMd5(md5)
}

fun FireApiFile.asFireFile(path: String, attributes: List<Attribute>): FireFile =
    FireFile(
        filePath = path,
        relPath = "Files/$path",
        fireId = fireOid,
        md5 = objectMd5,
        type = ExtFileType.FILE,
        size = objectSize.toLong(),
        attributes = attributes.toExtAttributes(FILES_RESERVED_ATTRS)
    )
