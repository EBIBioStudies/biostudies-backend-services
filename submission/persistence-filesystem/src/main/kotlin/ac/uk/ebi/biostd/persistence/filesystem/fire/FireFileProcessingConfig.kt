package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.request.Md5Path
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireWebClient

private val logger = KotlinLogging.logger {}

data class FireFileProcessingConfig(
    val relPath: String,
    val fireWebClient: FireWebClient,
    val previousFiles: Map<Md5Path, ExtFile>
)

fun FireFileProcessingConfig.processNfsFile(nfsFile: NfsFile): FireFile {
    logger.info { "processing file ${nfsFile.fileName}" }

    return findPreviousFile(nfsFile) ?: saveFile(nfsFile)
}

fun FireFileProcessingConfig.processFireFile(fireFile: FireFile): ExtFile {
    // TODO Remove in case it's definitively not necessary
    return fireFile
}

private fun FireFileProcessingConfig.findPreviousFile(
    nfsFile: NfsFile
) = previousFiles[Pair(nfsFile.md5, nfsFile.fileName)] as FireFile?

private fun FireFileProcessingConfig.saveFile(nfsFile: NfsFile): FireFile {
    val persisted = fireWebClient.save(nfsFile.file, nfsFile.md5)
    fireWebClient.setPath(persisted.fireOid, "$relPath/${nfsFile.fileName}")

    return FireFile(
        nfsFile.fileName,
        persisted.fireOid,
        persisted.objectMd5,
        persisted.objectSize.toLong(),
        nfsFile.attributes
    )
}
