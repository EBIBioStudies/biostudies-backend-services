package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import ac.uk.ebi.biostd.persistence.doc.model.RequestDocFile
import ebi.ac.uk.base.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.RequestFile
import org.bson.types.ObjectId

internal fun Either<ExtFile, ExtFileTable>.toDocFiles() = bimap(ExtFile::toDocFile, ExtFileTable::toDocFileTable)

class ToDocFileListMapper {
    internal fun convert(extFileList: ExtFileList): DocFileList {
        val pageTabFiles = extFileList.pageTabFiles.map { it.toDocFile() }
        return DocFileList(extFileList.filePath, pageTabFiles)
    }

    internal fun toDocFile(
        file: ExtFile,
        fileListName: String,
        index: Int,
        submissionId: ObjectId,
        submissionAccNo: String,
        submissionVersion: Int,
    ): FileListDocFile =
        FileListDocFile(
            id = ObjectId(),
            submissionId = submissionId,
            file = file.toDocFile(),
            fileListName = fileListName,
            index = index,
            submissionAccNo = submissionAccNo,
            submissionVersion = submissionVersion,
        )
}

private fun ExtFileTable.toDocFileTable() = DocFileTable(files.map { it.toDocFile() })

fun ExtFile.toDocFile(): DocFile =
    when (this) {
        is FireFile -> {
            FireDocFile(
                fileName = fileName,
                filePath = filePath,
                relPath = relPath,
                fireId = fireId,
                attributes = attributes.map { it.toDocAttribute() },
                md5 = md5,
                fileSize = size,
                fileType = type.value,
            )
        }

        is NfsFile -> {
            NfsDocFile(
                fileName = fileName,
                filePath = filePath,
                relPath = relPath,
                fullPath = fullPath,
                fileType = type.value,
                attributes = attributes.map { it.toDocAttribute() },
                md5 = md5,
                fileSize = size,
                source = source,
            )
        }

        is RequestFile -> {
            RequestDocFile(filePath, attributes.map { it.toDocAttribute() }, type)
        }
    }
