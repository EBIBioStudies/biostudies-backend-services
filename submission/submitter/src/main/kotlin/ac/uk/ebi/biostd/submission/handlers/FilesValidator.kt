package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.submission.exceptions.InvalidFilesException
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.allFiles
import ebi.ac.uk.util.collections.ifNotEmpty

class FilesValidator {
    fun validate(submission: ExtendedSubmission, filesSource: FilesSource) {
        validateFiles(submission, filesSource)
    }

    private fun validateFiles(submission: ExtendedSubmission, filesSource: FilesSource) {
        submission.allFiles()
            .filter { file -> filesSource.exists(file.path).not() }
            .ifNotEmpty { throw InvalidFilesException(it) }
    }
}
