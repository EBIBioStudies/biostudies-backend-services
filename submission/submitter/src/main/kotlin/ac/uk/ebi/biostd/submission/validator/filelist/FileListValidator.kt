package ac.uk.ebi.biostd.submission.validator.filelist

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.exceptions.InvalidFilesException
import ebi.ac.uk.errors.FileNotFoundException
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.util.collections.ifNotEmpty
import java.io.InputStream

class FileListValidator(
    private val serializationService: SerializationService
) {

    /**
     * Validate the given file list by deserializing file and checking each file presence using the given file source.
     * Note that in case of missing files only first 1000 are reported.
     */
    fun validateFileList(fileName: String, filesSource: FilesSource) {
        val file = filesSource.getFile(fileName) ?: throw FileNotFoundException(fileName)
        file.inputStream().use { validateFileList(it, SubFormat.fromFile(file), filesSource) }
    }

    private fun validateFileList(inputStream: InputStream, format: SubFormat, filesSource: FilesSource) {
        serializationService
            .deserializeFileList(inputStream, format)
            .filter { filesSource.getExtFile(it.path) == null }
            .take(fileListLimit)
            .toList()
            .ifNotEmpty { throw InvalidFilesException(it) }
    }

    companion object {
        private const val fileListLimit = 1000
    }
}
