package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.exception.InvalidFileListException
import ac.uk.ebi.biostd.exception.InvalidFileListException.Companion.directoryCantBeFileList
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.validation.InvalidChunkSizeException
import ebi.ac.uk.errors.FileNotFoundException
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allSections
import ebi.ac.uk.model.extensions.fileListName
import java.io.File
import java.lang.ClassCastException

internal class FileListSerializer(
    private val serializer: PagetabSerializer
) {
    internal fun deserializeFileList(fileName: String, source: FilesSource): FileList = getFileList(fileName, source)

    internal fun deserializeFileList(submission: Submission, source: FilesSource): Submission {
        submission.allSections()
            .filter { section -> section.fileListName != null }
            .map { section -> section to section.fileListName!! }
            .forEach { (section, fileList) -> section.fileList = getFileList(fileList, source) }
        return submission
    }

    private fun getFileList(fileList: String, source: FilesSource): FileList {
        val filesTable = getFilesTable(getFile(fileList, source))
        return FileList(fileList, filesTable.elements)
    }

    private fun getFile(fileList: String, source: FilesSource): File {
        return when (val file = source.getFile(fileList)) {
            null -> throw FileNotFoundException(fileList)
            else -> if (file.isFile) file else throw directoryCantBeFileList(fileList)
        }
    }

    private fun getFilesTable(file: File): FilesTable =
        runCatching { file.inputStream().use { serializer.deserializeFileList(it, SubFormat.fromFile(file)) } }
            .getOrElse { throw InvalidFileListException(file.name, errorMsg(it)) }

    private fun errorMsg(exception: Throwable) = when (exception) {
        is ClassCastException,
        is InvalidChunkSizeException -> "The provided page tab doesn't match the file list format"
        else -> exception.message.orEmpty()
    }
}
