package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable

internal const val TO_LIBRARY_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToFileListKt"

class ToFileList {
    fun convert(extFileList: ExtFileList): FileList =
        FileList(extFileList.filePath, extFileList.files.map { it.toFile() })
}

class ToFilesTable(private val toFileList: ToFileList) {
    fun convert(extFileList: ExtFileList) = FilesTable(toFileList.convert(extFileList).referencedFiles)
}
