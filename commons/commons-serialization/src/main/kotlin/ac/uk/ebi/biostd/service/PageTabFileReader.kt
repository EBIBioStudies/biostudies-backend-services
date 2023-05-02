package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.exception.EmptyPageTabFileException
import ac.uk.ebi.biostd.exception.InvalidFileListException
import ebi.ac.uk.errors.FilesProcessingException
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.util.file.ExcelReader.asTsv
import java.io.File

object PageTabFileReader {
    fun readAsPageTab(file: File): File {
        require(file.size() > 0) { throw EmptyPageTabFileException(file.name) }
        return if (file.extension == "xlsx") asTsv(file) else file
    }

    fun getFileListFile(
        fileListName: String,
        filesSource: FileSourcesList,
    ): File = when (val file = filesSource.getFileList(fileListName)) {
        null -> throw FilesProcessingException(fileListName, filesSource)
        else -> when {
            file.isFile.not() -> throw InvalidFileListException.directoryCantBeFileList(fileListName)
            file.extension == "xlsx" -> asTsv(file)
            else -> file
        }
    }
}
