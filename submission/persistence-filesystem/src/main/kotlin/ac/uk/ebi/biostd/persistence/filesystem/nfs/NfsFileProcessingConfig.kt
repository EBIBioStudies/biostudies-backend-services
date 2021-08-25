package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.notExist
import mu.KotlinLogging
import java.io.File
import java.nio.file.attribute.PosixFilePermission

private val logger = KotlinLogging.logger {}

data class NfsFileProcessingConfig(
    val mode: FileMode,
    val subFolder: File,
    val tempFolder: File,
    val filePermissions: Set<PosixFilePermission>,
    val dirPermissions: Set<PosixFilePermission>
)

fun NfsFileProcessingConfig.nfsCopy(extFile: NfsFile): NfsFile {
    val source = if (extFile.file.startsWith(subFolder)) tempFolder.resolve(extFile.fileName) else extFile.file
    val target = subFolder.resolve(extFile.fileName)
    val current = tempFolder.resolve(extFile.fileName)

    logger.info { "copying file ${source.absolutePath} into ${target.absolutePath}" }

    when {
        current.exists() && source.md5() == current.md5() -> FileUtils.moveFile(
            current,
            target,
            filePermissions,
            dirPermissions
        )
        else -> FileUtils.copyOrReplaceFile(source, target, filePermissions, dirPermissions)
    }

    return extFile.copy(file = target)
}

fun NfsFileProcessingConfig.nfsMove(extFile: NfsFile): NfsFile {
    val source = if (extFile.file.startsWith(subFolder)) tempFolder.resolve(extFile.fileName) else extFile.file
    val target = subFolder.resolve(extFile.fileName)

    if (target.notExist()) {
        logger.info { "moving file ${source.absolutePath} into ${target.absolutePath}" }
        FileUtils.moveFile(source, target, filePermissions, dirPermissions)
    }

    return extFile.copy(file = target)
}
