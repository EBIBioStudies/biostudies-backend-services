package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocFileRef
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.FileSystem.FIRE
import ac.uk.ebi.biostd.persistence.doc.model.FileSystem.FIRE_DIR
import ac.uk.ebi.biostd.persistence.doc.model.FileSystem.NFS
import ac.uk.ebi.biostd.persistence.doc.model.FireDocDirectory
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.size
import org.bson.types.ObjectId
import java.io.File

internal fun Either<ExtFile, ExtFileTable>.toDocFiles() = bimap(ExtFile::toDocFile, ExtFileTable::toDocFileTable)
internal fun ExtFileList.toDocFileList(submissionId: ObjectId): Pair<DocFileList, List<FileListDocFile>> {
    val listFiles = files.map { toFileDocListFile(submissionId, it) }
    val listRef = listFiles.map { DocFileRef(fileId = it.id) }

    return Pair(DocFileList(fileName, listRef), listFiles)
}

private fun toFileDocListFile(submissionId: ObjectId, extFile: ExtFile) = when (extFile) {
    is FireFile -> FileListDocFile(
        id = ObjectId(),
        submissionId = submissionId,
        fileName = extFile.fileName,
        fullPath = extFile.fireId,
        attributes = extFile.attributes.map { it.toDocAttribute() },
        md5 = extFile.md5,
        size = extFile.size,
        fileSystem = FIRE
    )
    is FireDirectory -> FileListDocFile(
        id = ObjectId(),
        submissionId = submissionId,
        fileName = extFile.fileName,
        fullPath = extFile.fileName,
        attributes = extFile.attributes.map { it.toDocAttribute() },
        md5 = extFile.md5,
        size = extFile.size,
        fileSystem = FIRE_DIR
    )
    is NfsFile -> FileListDocFile(
        id = ObjectId(),
        submissionId = submissionId,
        fileName = extFile.fileName,
        fullPath = extFile.file.absolutePath,
        attributes = extFile.attributes.map { it.toDocAttribute() },
        md5 = extFile.md5,
        size = extFile.size,
        fileSystem = NFS
    )
}

private fun ExtFileTable.toDocFileTable() = DocFileTable(files.map { it.toDocFile() })
private fun fileType(file: File): String = if (file.isDirectory) "directory" else "file"
internal fun ExtFile.toDocFile(): DocFile = when (this) {
    is FireFile -> FireDocFile(
        fileName = fileName,
        fireId = fireId,
        attributes = attributes.map { it.toDocAttribute() },
        md5 = md5,
        fileSize = size,
    )
    is FireDirectory -> FireDocDirectory(
        fileName = fileName,
        attributes = attributes.map { it.toDocAttribute() },
        md5 = md5,
        fileSize = size,
    )
    is NfsFile -> NfsDocFile(
        relPath = fileName,
        fullPath = file.absolutePath,
        fileType = fileType(file),
        attributes = attributes.map { it.toDocAttribute() },
        md5 = md5,
        fileSize = file.size(),
    )
}

class FireFileToFileListDocFileNotSupportedException : UnsupportedOperationException()
