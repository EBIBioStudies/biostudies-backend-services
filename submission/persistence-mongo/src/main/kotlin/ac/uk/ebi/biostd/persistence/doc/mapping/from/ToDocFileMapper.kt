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
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.io.InputStream

internal fun Either<ExtFile, ExtFileTable>.toDocFiles() = bimap(ExtFile::toDocFile, ExtFileTable::toDocFileTable)

class ToDocFileListMapper(
    private val serializationService: ExtSerializationService,
) {
    internal fun convert(
        extFileList: ExtFileList,
        subId: ObjectId,
        accNo: String,
        version: Int,
    ): Pair<DocFileList, List<FileListDocFile>> {
        val listFiles = extFileList.file.inputStream().use { getFiles(it, extFileList.filePath, subId, accNo, version) }
        val pageTabFiles = extFileList.pageTabFiles.map { it.toDocFile() }
        return Pair(DocFileList(extFileList.filePath, pageTabFiles), listFiles)
    }

    private fun getFiles(
        stream: InputStream,
        path: String,
        subId: ObjectId,
        accNo: String,
        version: Int,
    ): List<FileListDocFile> =
        serializationService
            .deserializeListAsSequence(stream)
            .mapIndexed { idx, file -> FileListDocFile(ObjectId(), subId, file.toDocFile(), path, idx, version, accNo) }
            .toList()
}

private fun ExtFileTable.toDocFileTable() = DocFileTable(files.map { it.toDocFile() })

internal fun ExtFile.toDocFile(): DocFile =
    when (this) {
        is FireFile ->
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

        is NfsFile ->
            NfsDocFile(
                fileName = fileName,
                filePath = filePath,
                relPath = relPath,
                fullPath = fullPath,
                fileType = type.value,
                attributes = attributes.map { it.toDocAttribute() },
                md5 = md5,
                fileSize = size,
            )

        is RequestFile -> RequestDocFile(filePath = filePath, attributes = attributes.map { it.toDocAttribute() })
    }
