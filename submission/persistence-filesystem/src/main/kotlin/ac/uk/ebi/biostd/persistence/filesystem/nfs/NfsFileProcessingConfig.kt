package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.FileUtils.copyOrReplaceFile
import ebi.ac.uk.io.FileUtils.moveFile
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.ext.md5
import java.io.File

data class NfsFileProcessingConfig(
    val mode: FileMode,
    val subFolder: File,
    val targetFolder: File,
    val permissions: Permissions
)

fun NfsFileProcessingConfig.nfsCopy(extFile: NfsFile): NfsFile {
    val target = targetFolder.resolve(extFile.fileName)
    val subFile = subFolder.resolve(extFile.fileName)

    when {
        subFile.exists() && subFile.md5() == extFile.md5 -> moveFile(subFile, target, permissions)
        target.exists().not() -> copyOrReplaceFile(extFile.file, target, permissions)
    }

    return extFile.copy(file = subFolder.resolve(extFile.fileName))
}

fun NfsFileProcessingConfig.nfsMove(extFile: NfsFile): NfsFile {
    val target = targetFolder.resolve(extFile.fileName)
    if (target.exists().not()) moveFile(extFile.file, target, permissions)
    return extFile.copy(file = subFolder.resolve(extFile.fileName))
}
