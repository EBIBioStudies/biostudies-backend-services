package uk.ac.ebi.biostd.service

import uk.ac.ebi.biostd.errors.FilesProcessingException
import uk.ac.ebi.biostd.exception.EmptyPageTabFileException
import uk.ac.ebi.biostd.exception.InvalidFileListException
import uk.ac.ebi.biostd.integration.SubFormat.Companion.XLSX_EXTENSION
import uk.ac.ebi.biostd.integration.SubFormat.Companion.checkFileListExtension
import uk.ac.ebi.biostd.io.ext.size
import uk.ac.ebi.biostd.io.sources.FileSourcesList
import uk.ac.ebi.biostd.util.file.ExcelReader.asTsv
import java.io.File

object PageTabFileReader {
    fun readAsPageTab(file: File): File {
        require(file.size() > 0) { throw EmptyPageTabFileException(file.name) }
        return if (file.extension == XLSX_EXTENSION) asTsv(file) else file
    }

    suspend fun getFileListFile(
        fileListName: String,
        filesSource: FileSourcesList,
    ): File {
        checkFileListExtension(fileListName)
        return when (val file = filesSource.getFileList(fileListName)) {
            null -> throw FilesProcessingException(fileListName, filesSource)
            else ->
                when {
                    file.isFile.not() -> throw InvalidFileListException.directoryCantBeFileList(fileListName)
                    file.extension == XLSX_EXTENSION -> asTsv(file)
                    else -> file
                }
        }
    }
}
