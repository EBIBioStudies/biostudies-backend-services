package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.exception.InvalidFileListException
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.validation.InvalidChunkSizeException
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.FireBioFile
import ebi.ac.uk.io.sources.FireDirectoryBioFile
import ebi.ac.uk.io.sources.NfsBioFile
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allSections
import ebi.ac.uk.model.extensions.fileListName
import java.io.File

internal class FileListSerializer(
    private val serializer: PagetabSerializerImpl
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
        checkFileList(getFile(fileList, source))
        return FileList(fileList)
    }

    private fun getFile(fileList: String, source: FilesSource): File {
        return when (val bioFile = source.getFile(fileList)) {
            is FireBioFile -> TODO()
            is FireDirectoryBioFile -> TODO()
            is NfsBioFile -> bioFile.file
        }
    }

    private fun checkFileList(file: File) {
        try {
            file.inputStream().use { serializer.deserializeFileList(it, SubFormat.fromFile(file)) }
        } catch (exception: Exception) {
            throw InvalidFileListException("Problem processing file list '${file.name}': ${errorMsg(exception)}")
        }
    }

    private fun errorMsg(exception: Throwable) = when (exception) {
        is ClassCastException,
        is InvalidChunkSizeException -> "The provided page tab doesn't match the file list format"
        else -> exception.message
    }
}
