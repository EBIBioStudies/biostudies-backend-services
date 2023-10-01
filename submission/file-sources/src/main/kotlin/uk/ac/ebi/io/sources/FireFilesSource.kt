package uk.ac.ebi.io.sources

import ebi.ac.uk.extended.mapping.from.toExtAttributes
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FileFields
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File

internal class FireFilesSource(
    private val fireClient: FireClient,
) : FilesSource {
    override suspend fun getExtFile(path: String, type: String, attributes: List<Attribute>): ExtFile? {
        val md5Attribute = attributes.firstOrNull { it.name == FileFields.DB_MD5.value }

        return when (val md5 = md5Attribute?.value) {
            null -> null
            else -> fireClient.findByMd5Sync(md5).first().asFireFile(path, attributes)
        }
    }

    override suspend fun getFileList(path: String): File? = null

    override val description: String = "EBI internal files Archive"
}

private fun FireApiFile.asFireFile(filePath: String, attributes: List<Attribute>): FireFile =
    FireFile(
        fireId = fireOid,
        firePath = path,
        published = published,
        filePath = filePath,
        relPath = "Files/$filePath",
        md5 = objectMd5,
        size = objectSize,
        type = ebi.ac.uk.extended.model.ExtFileType.FILE,
        attributes = attributes.toExtAttributes(ebi.ac.uk.model.constants.FILES_RESERVED_ATTRS)
    )
