package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocFileRef
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.FileSystem.NFS
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
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
    is FireFile -> TODO()
    is NfsFile -> FileListDocFile(
        id = ObjectId(),
        submissionId = submissionId,
        fileName = extFile.fileName,
        fullPath = extFile.file.absolutePath,
        attributes = extFile.attributes.map { it.toDocAttribute() },
        md5 = extFile.md5,
        fileSystem = NFS
    )
}

private fun ExtFileTable.toDocFileTable() = DocFileTable(files.map { it.toDocFile() })
private fun fileType(file: File): String = if (file.isDirectory) "directory" else "file"
private fun ExtFile.toDocFile(): DocFile = when (this) {
    is FireFile -> TODO()
    is NfsFile -> DocFile(
        fileName,
        file.absolutePath,
        attributes.map { it.toDocAttribute() },
        md5,
        fileType(file),
        file.size(),
        NFS
    )
}
