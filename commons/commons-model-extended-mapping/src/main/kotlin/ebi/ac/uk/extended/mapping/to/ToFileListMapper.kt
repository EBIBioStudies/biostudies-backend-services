package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

class ToFileListMapper(
    private val serializationService: ExtSerializationService
) {
    fun convert(fileList: ExtFileList): FileList {
        val fileStream = fileList.file.inputStream()
        val files = fileStream.use { stream -> serializationService.deserialize(stream).map { it.toFile() } }
        return FileList(fileList.filePath, files.toList())
    }
}

class ToFilesTableMapper(private val toFileListMapper: ToFileListMapper) {
    fun convert(fileList: ExtFileList) = FilesTable(toFileListMapper.convert(fileList).referencedFiles)
}
