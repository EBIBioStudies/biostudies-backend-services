package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.FileUtils.moveFile
import mu.KotlinLogging
import java.io.File
import java.nio.file.attribute.PosixFilePermission

private val logger = KotlinLogging.logger {}

data class NfsFileProcessingConfig(
    val mode: FileMode,
    val subFolder: File,
    val targetFolder: File,
    val filePermissions: Set<PosixFilePermission>,
    val dirPermissions: Set<PosixFilePermission>
)

fun NfsFileProcessingConfig.nfsCopy(extFile: NfsFile): NfsFile {
    val target = targetFolder.resolve(extFile.fileName)
    if (target.exists().not()) FileUtils.copyOrReplaceFile(extFile.file, target, filePermissions, dirPermissions)
    return extFile.copy(file = subFolder.resolve(extFile.fileName))
}

fun NfsFileProcessingConfig.nfsMove(extFile: NfsFile): NfsFile {
    val target = targetFolder.resolve(extFile.fileName)
    if (target.exists().not()) moveFile(extFile.file, target, filePermissions, dirPermissions)
    return extFile.copy(file = subFolder.resolve(extFile.fileName))
}

