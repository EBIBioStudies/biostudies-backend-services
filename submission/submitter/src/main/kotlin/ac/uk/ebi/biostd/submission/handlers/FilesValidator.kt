package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.submission.exceptions.InvalidFilesException
import ac.uk.ebi.biostd.submission.exceptions.InvalidLibraryFileException
import ac.uk.ebi.biostd.submission.model.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.allFiles
import ebi.ac.uk.model.extensions.libFileSections
import ebi.ac.uk.model.extensions.libraryFile
import ebi.ac.uk.util.collections.ifNotEmpty

class FilesValidator {
    fun validate(submission: ExtendedSubmission, filesSource: FilesSource) {
        validateFiles(submission, filesSource)
        validateLibraryFiles(submission, filesSource)
    }

    private fun validateLibraryFiles(submission: ExtendedSubmission, filesSource: FilesSource) {
        submission.libFileSections()
            .filterNot { section -> filesSource.exists(section.libraryFile!!) }
            .ifNotEmpty { throw InvalidLibraryFileException(it) }
    }

    private fun validateFiles(submission: ExtendedSubmission, filesSource: FilesSource) {
        submission.allFiles()
            .filter { file -> filesSource.exists(file.path).not() }
            .ifNotEmpty { throw InvalidFilesException(it, INVALID_FILES_ERROR_MSG) }
    }
}
