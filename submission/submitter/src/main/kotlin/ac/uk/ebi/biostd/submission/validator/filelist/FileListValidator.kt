package ac.uk.ebi.biostd.submission.validator.filelist

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.service.PageTabFileReader.getFileListFile
import ac.uk.ebi.biostd.submission.exceptions.InvalidFilesException
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.util.collections.ifNotEmpty

class FileListValidator(
    private val serializationService: SerializationService
) {
    /**
     * Validates the given file list by deserializing it and checking each file presence using the given file source.
     * Note that in case of missing files only first 1000 are reported.
     */
    fun validateFileList(fileName: String, filesSource: FilesSource) {
        val fileListFile = getFileListFile(fileName, filesSource)
        fileListFile.inputStream().use { inputStream ->
            serializationService
                .deserializeFileList(inputStream, SubFormat.fromFile(fileListFile))
                .filter { filesSource.getExtFile(it.path) == null }
                .take(fileListLimit)
                .toList()
                .ifNotEmpty { throw InvalidFilesException(it) }
        }
    }

    companion object {
        private const val fileListLimit = 1000
    }
}
