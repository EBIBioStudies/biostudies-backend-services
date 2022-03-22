package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable

class ToFileListMapper {
    fun convert(fileList: ExtFileList): FileList = FileList(fileList.filePath, fileList.files.map { it.toFile() })
}

class ToFilesTableMapper(private val toFileListMapper: ToFileListMapper) {
    fun convert(fileList: ExtFileList) = FilesTable(toFileListMapper.convert(fileList).referencedFiles)
}
