package ac.uk.ebi.biostd.submission.validator.filelist

import ac.uk.ebi.biostd.service.FileListSerializer
import ac.uk.ebi.biostd.submission.exceptions.InvalidFilesException
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.util.collections.ifNotEmpty

class FileListValidator(
    private val fileListSerializer: FileListSerializer
) {
    fun validateFileList(fileName: String, filesSource: FilesSource) {
        fileListSerializer
            .deserializeFileList(fileName, filesSource)
            .referencedFiles
            .filterNot { filesSource.exists(it.path) }
            .ifNotEmpty { throw InvalidFilesException(it) }
    }
}
