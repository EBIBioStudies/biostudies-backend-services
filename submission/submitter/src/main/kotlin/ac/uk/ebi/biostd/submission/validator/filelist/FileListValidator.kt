package ac.uk.ebi.biostd.submission.validator.filelist

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.exception.FileListNotFoundException
import ebi.ac.uk.base.ifFalse
import ebi.ac.uk.io.sources.FilesSource

class FileListValidator(
    private val serializationService: SerializationService
) {
    fun validateFileList(fileName: String, filesSource: FilesSource) {
        serializationService
            .deserializeFileList(fileName, filesSource)
            .let { filesSource.exists(fileName) }
            .ifFalse { throw FileListNotFoundException(fileName) }
    }
}
