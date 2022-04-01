package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.common.properties.PersistenceProperties
import ebi.ac.uk.io.sources.BioFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.FilesSource.Companion.EMPTY_FILE_SOURCE
import ebi.ac.uk.io.sources.FireBioFile
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import uk.ac.ebi.fire.client.model.FireFile
import uk.ac.ebi.fire.client.model.isAvailable
import java.nio.file.Path

class FireFilesSourceFactory(
    private val props: PersistenceProperties,
    private val fireWebClient: FireWebClient
) {
    fun createFireSource(): FilesSource = if (props.enableFire) FireFilesSource(fireWebClient) else EMPTY_FILE_SOURCE

    fun createSubmissionFireSource(accNo: String, basePath: Path): FilesSource =
        if (props.enableFire) SubmissionFireFilesSource(fireWebClient, accNo, basePath) else EMPTY_FILE_SOURCE
}

// TODO unit tests
class FireFilesSource(
    private val fireWebClient: FireWebClient
) : FilesSource {
    override fun getFile(path: String, md5: String?): BioFile? {
        return when (md5) {
            null -> null
            else -> fireWebClient.findByMd5(md5)
                .firstOrNull { it.isAvailable() }
                ?.let { it.asFireBioFile(path, lazy { fireWebClient.downloadByFireId(it.fireOid, path).readText() }) }
        }
    }
}

private class SubmissionFireFilesSource(
    private val fireWebClient: FireWebClient,
    private val accNo: String,
    private val basePath: Path
) : FilesSource {
    override fun getFile(path: String, md5: String?): FireBioFile? {
        if (md5 == null) {
            return fireWebClient.findByPath(basePath.resolve(path).toString())
                ?.takeIf { it.isAvailable(accNo) }
                ?.let { it.asFireBioFile(path, lazy { fireWebClient.downloadByFireId(it.fireOid, path).readText() }) }
        }

        return fireWebClient.findByMd5(md5)
            .firstOrNull { it.isAvailable(accNo) }
            ?.let { it.asFireBioFile(path, lazy { fireWebClient.downloadByFireId(it.fireOid, path).readText() }) }
    }
}

fun FireFile.asFireBioFile(path: String, readContent: Lazy<String>): FireBioFile =
    FireBioFile(fireOid, path, objectMd5, objectSize.toLong(), readContent)
