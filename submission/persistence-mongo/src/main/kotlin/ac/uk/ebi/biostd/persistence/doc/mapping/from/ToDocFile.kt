package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
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

internal fun ExtFileList.toDocFileList(
    submissionId: ObjectId,
    accNo: String,
    version: Int
): Pair<DocFileList, List<FileListDocFile>> {
    val listFiles =
        files.mapIndexed { index, file -> toFileDocListFile(submissionId, accNo, fileName, version, index, file) }
    val pageTabFiles = pageTabFiles.map { it.toDocFile() }

    return Pair(DocFileList(filePath, pageTabFiles), listFiles)
}

@Suppress("LongParameterList")
private fun toFileDocListFile(
    submissionId: ObjectId,
    submissionAccNo: String,
    fileName: String,
    version: Int,
    index: Int,
    extFile: ExtFile
): FileListDocFile =
    FileListDocFile(
        id = ObjectId(),
        submissionId = submissionId,
        submissionAccNo = submissionAccNo,
        submissionVersion = version,
        file = extFile.toDocFile(),
        index = index,
        fileListName = fileName
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
