package ac.uk.ebi.biostd.submission.validator.filelist

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.submission.exceptions.InvalidFilesException
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.util.collections.ifNotEmpty

class FileListValidator(
    private val serializationService: SerializationService
) {
    fun validateFileList(fileName: String, filesSource: FilesSource) {
        serializationService
            .deserializeFileList(fileName, filesSource)
            .referencedFiles
            .filter { filesSource.getFile(it.path) == null }
            .ifNotEmpty { throw InvalidFilesException(it) }
    }
}
