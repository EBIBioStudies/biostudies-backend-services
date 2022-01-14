package ac.uk.ebi.biostd.submission.validator.filelist

import ac.uk.ebi.biostd.integration.PageTabFileReader
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.exceptions.InvalidFilesException
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.util.collections.ifNotEmpty
import java.io.FileInputStream

class FileListValidator(
    private val serializationService: SerializationService
) {
    fun validateFileList(fileName: String, filesSource: FilesSource) {
        val (file, format) = PageTabFileReader.getPageTabFile(filesSource.getSystemFile(fileName))
        file.inputStream().use { checkFiles(it, fileName, format, filesSource) }
    }

    private fun checkFiles(
        inputStream: FileInputStream,
        fileName: String,
        format: SubFormat,
        filesSource: FilesSource
    ) {
        serializationService
            .deserializeFileList(inputStream, format)
            .filterNot { filesSource.exists(it.path) }
            .take(FILES_TO_REPORT)
            .toList()
            .ifNotEmpty { throw InvalidFilesException(fileName, it) }
    }

    companion object {
        const val FILES_TO_REPORT = 100
    }
}
