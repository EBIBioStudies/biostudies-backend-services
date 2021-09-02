package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.*
import ac.uk.ebi.biostd.persistence.doc.model.FileSystem.FIRE
import ac.uk.ebi.biostd.persistence.doc.model.FileSystem.NFS
import arrow.core.Either
import ebi.ac.uk.extended.model.*
import java.nio.file.Paths

internal fun DocFile.toExtFile(): ExtFile = when (this) {
    is FireDocFile -> FireFile(fileName, fireId, md5, fileSize, attributes.toExtAttributes())
    is NfsDocFile -> NfsFile(relPath, Paths.get(fullPath).toFile(), attributes.toExtAttributes())
}

internal fun DocFileTable.toExtFileTable(): ExtFileTable = ExtFileTable(files.map { it.toExtFile() })

internal fun Either<DocFile, DocFileTable>.toExtFiles(): Either<ExtFile, ExtFileTable> =
    bimap({ it.toExtFile() }) { it.toExtFileTable() }

internal fun FileListDocFile.toExtFile(): ExtFile = when (fileSystem) {
    FIRE -> FireFile(fileName, location, md5, size, attributes.toExtAttributes())
    NFS -> NfsFile(fileName, Paths.get(location).toFile(), attributes.toExtAttributes()).also { it.md5 = md5 }
}

/**
 * Maps a DocFileList to corresponding Ext type. Note that empty list is used as files as list files are not loaded as
 * part of the submission.
 */
internal fun DocFileList.toExtFileList() = ExtFileList(fileName, emptyList())
