package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocFileRef
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
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
    val pageTabFiles = pageTabFiles.map { it.toDocFile() }

    return Pair(DocFileList(fileName, listRef, pageTabFiles), listFiles)
}

private fun toFileDocListFile(submissionId: ObjectId, extFile: ExtFile): FileListDocFile =
    FileListDocFile(
        id = ObjectId(),
        submissionId = submissionId,
        file = extFile.toDocFile()
    )

private fun ExtFileTable.toDocFileTable() = DocFileTable(files.map { it.toDocFile() })
private fun fileType(file: File): String = if (file.isDirectory) "directory" else "file"
internal fun ExtFile.toDocFile(): DocFile = when (this) {
    is FireFile -> FireDocFile(
        fileName = fileName,
        filePath = filePath,
        relPath = relPath,
        fireId = fireId,
        attributes = attributes.map { it.toDocAttribute() },
        md5 = md5,
        fileSize = size,
    )
    is FireDirectory -> FireDocDirectory(
        fileName = fileName,
        filePath = filePath,
        relPath = relPath,
        attributes = attributes.map { it.toDocAttribute() },
        md5 = md5,
        fileSize = size,
    )
    is NfsFile -> NfsDocFile(
        fileName = fileName,
        filePath = filePath,
        relPath = relPath,
        fullPath = file.absolutePath,
        fileType = fileType(file),
        attributes = attributes.map { it.toDocAttribute() },
        md5 = md5,
        fileSize = file.size(),
    )
}
