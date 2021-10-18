package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.request.FileProcessingConfig
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.notExist
import ebi.ac.uk.io.ext.size
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun FileProcessingConfig.nfsCopy(extFile: NfsFile): NfsFile {
    val file = extFile.file
    val source = if (file.startsWith(subFolder)) tempFolder.resolve(extFile.fileName) else file
    val target = subFolder.resolve(extFile.fileName)
    val current = tempFolder.resolve(extFile.fileName)

    logger.info { "$accNo $owner Copying file $file with size ${file.size()} into ${target.absolutePath}" }

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

fun FileProcessingConfig.nfsMove(extFile: NfsFile): NfsFile {
    val file = extFile.file
    val source = if (file.startsWith(subFolder)) tempFolder.resolve(extFile.fileName) else file
    val target = subFolder.resolve(extFile.fileName)

    if (target.notExist()) {
        logger.info { "$accNo $owner Moving file $file with size ${file.size()} into ${target.absolutePath}" }
        FileUtils.moveFile(source, target, filePermissions, dirPermissions)
    }

    return extFile.copy(file = target)
}
