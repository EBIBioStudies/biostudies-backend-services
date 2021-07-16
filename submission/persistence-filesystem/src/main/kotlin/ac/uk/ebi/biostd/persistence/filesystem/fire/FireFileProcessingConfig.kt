package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.request.Md5
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireWebClient

private val logger = KotlinLogging.logger {}

data class FireFileProcessingConfig(
    val relPath: String,
    val fireWebClient: FireWebClient,
    val previousFiles: Map<Md5, ExtFile>
)

fun FireFileProcessingConfig.processNfsFile(nfsFile: NfsFile): FireFile {
    logger.info { "processing file ${nfsFile.fileName}" }

    return reusePreviousFile(nfsFile) ?: saveFile(nfsFile)
}

fun FireFileProcessingConfig.processFireFile(fireFile: FireFile): ExtFile {
    // TODO Remove in case it's definitively not necessary
    return fireFile
}

private fun FireFileProcessingConfig.reusePreviousFile(nfsFile: NfsFile): FireFile? {
    val previousFile = previousFiles[nfsFile.md5] as FireFile?
    return previousFile?.let {
        FireFile(
            nfsFile.fileName,
            it.fireId,
            it.md5,
            it.size,
            nfsFile.attributes
        )
    }
}

private fun FireFileProcessingConfig.saveFile(nfsFile: NfsFile): FireFile {
    val persisted = fireWebClient.save(nfsFile.file, nfsFile.md5)
    return FireFile(
        nfsFile.fileName,
        persisted.fireOid,
        persisted.objectMd5,
        persisted.objectSize.toLong(),
        nfsFile.attributes
    )
}
