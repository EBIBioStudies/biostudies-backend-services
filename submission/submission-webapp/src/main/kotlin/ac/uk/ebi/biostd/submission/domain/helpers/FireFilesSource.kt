package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.common.properties.PersistenceProperties
import ebi.ac.uk.extended.mapping.from.toExtAttributes
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.FilesSource.Companion.EMPTY_FILE_SOURCE
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FILES_RESERVED_ATTRS
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import uk.ac.ebi.fire.client.model.FireApiFile
import uk.ac.ebi.fire.client.model.isAvailable
import java.io.File
import java.nio.file.Path

class FireFilesSourceFactory(
    private val props: PersistenceProperties,
    private val fireWebClient: FireWebClient
) {
    fun createFireSource(): FilesSource = if (props.enableFire) FireFilesSource(fireWebClient) else EMPTY_FILE_SOURCE

    fun createSubmissionFireSource(accNo: String, basePath: Path): FilesSource =
        if (props.enableFire) SubmissionFireFilesSource(fireWebClient, accNo, basePath) else EMPTY_FILE_SOURCE
}

class FireFilesSource(
    private val fireWebClient: FireWebClient,
) : FilesSource {
    override fun getExtFile(
        path: String,
        md5: String?,
        attributes: List<Attribute>
    ): ExtFile? {
        return when (md5) {
            null -> null
            else -> fireWebClient.findByMd5(md5).firstOrNull { it.isAvailable() }?.asFireBioFile(path, attributes)
        }
    }

    override fun getFile(path: String, md5: String?): File? {
        return when (md5) {
            null -> null
            else -> fireWebClient.downloadByMd5(md5)
        }
    }
}

private class SubmissionFireFilesSource(
    private val fireWebClient: FireWebClient,
    private val accNo: String,
    private val basePath: Path
) : FilesSource {
    override fun getExtFile(
        path: String,
        md5: String?,
        attributes: List<Attribute>
    ): ExtFile? {
        if (md5 == null) {
            return fireWebClient.findByPath(basePath.resolve(path).toString())
                ?.takeIf { it.isAvailable(accNo) }
                ?.asFireBioFile(path, attributes)
        }

        return fireWebClient.findByMd5(md5).firstOrNull { it.isAvailable(accNo) }?.asFireBioFile(path, attributes)
    }

    override fun getFile(path: String, md5: String?): File? =
        if (md5 == null) fireWebClient.downloadByPath(path) else fireWebClient.downloadByMd5(md5)
}

fun FireApiFile.asFireBioFile(path: String, attributes: List<Attribute>): FireFile =
    FireFile(
        filePath = path,
        relPath = "Files/$path",
        fireId = fireOid,
        md5 = objectMd5,
        size = objectSize.toLong(),
        attributes = attributes.toExtAttributes(FILES_RESERVED_ATTRS)
    )