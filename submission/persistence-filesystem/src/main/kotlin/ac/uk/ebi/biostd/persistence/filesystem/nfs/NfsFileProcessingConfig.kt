package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.FileUtils.copyOrReplaceFile
import ebi.ac.uk.io.FileUtils.moveFile
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

data class NfsFileProcessingConfig(
    val accNo: String,
    val submitter: String,
    val mode: FileMode,
    val subFolder: File,
    val targetFolder: File,
    val permissions: Permissions
)

fun NfsFileProcessingConfig.nfsCopy(extFile: NfsFile): NfsFile {
    val file = extFile.file
    val target = targetFolder.resolve(extFile.fileName)
    val subFile = subFolder.resolve(extFile.fileName)

    logger.info { "$accNo $submitter Copying file $file with size ${file.size()} into ${target.absolutePath}" }

    when {
        target.exists().not() && subFile.exists() && subFile.md5() == extFile.md5 ->
            moveFile(subFile, target, permissions)
        target.exists().not() -> copyOrReplaceFile(file, target, permissions)
    }

    return extFile.copy(file = subFolder.resolve(extFile.fileName))
}

fun NfsFileProcessingConfig.nfsMove(extFile: NfsFile): NfsFile {
    val file = extFile.file
    val target = targetFolder.resolve(extFile.fileName)

    logger.info { "$accNo $submitter Moving file $file with size ${file.size()} into ${target.absolutePath}" }

    if (target.exists().not()) moveFile(file, target, permissions)
    return extFile.copy(file = subFolder.resolve(extFile.fileName))
}
