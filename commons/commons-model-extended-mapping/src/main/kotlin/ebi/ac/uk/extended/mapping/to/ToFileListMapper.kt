package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable

class ToFileListMapper {
    fun convert(extFileList: ExtFileList): FileList =
        FileList(extFileList.filePath, extFileList.files.map { it.toFile() })
}

class ToFilesTableMapper(private val toFileListMapper: ToFileListMapper) {
    fun convert(extFileList: ExtFileList) = FilesTable(toFileListMapper.convert(extFileList).referencedFiles)
}
