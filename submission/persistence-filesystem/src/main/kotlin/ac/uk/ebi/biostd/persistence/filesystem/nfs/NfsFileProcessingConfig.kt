package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.FileUtils.copyOrReplaceFile
import ebi.ac.uk.io.FileUtils.moveFile
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.notExist
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

data class NfsFileProcessingConfig(
    val accNo: String,
    val owner: String,
    val mode: FileMode,
    val subFolder: File,
    val targetFolder: File,
    val permissions: Permissions
)

fun NfsFileProcessingConfig.nfsCopy(extFile: NfsFile): NfsFile {
    val file = extFile.file
    val target = targetFolder.resolve(extFile.filePath)
    val subFile = subFolder.resolve(extFile.filePath)

    logger.info { "$accNo $owner Copying file $file with size ${extFile.size} into ${target.absolutePath}" }

    when {
        target.notExist() && subFile.exists() && subFile.md5() == extFile.md5 -> moveFile(subFile, target, permissions)
        target.notExist() -> copyOrReplaceFile(file, target, permissions)
    }

    return extFile.copy(fullPath = subFile.absolutePath, file = subFile)
}

fun NfsFileProcessingConfig.nfsMove(extFile: NfsFile): NfsFile {
    val file = extFile.file
    val target = targetFolder.resolve(extFile.filePath)
    val subFile = subFolder.resolve(extFile.filePath)

    logger.info { "$accNo $owner Moving file $file with size ${extFile.size} into ${target.absolutePath}" }
    if (target.notExist()) moveFile(extFile.file, target, permissions)

    return extFile.copy(fullPath = subFile.absolutePath, file = subFile)
}
