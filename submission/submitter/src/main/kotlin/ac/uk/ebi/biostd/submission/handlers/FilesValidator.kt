package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.submission.exceptions.InvalidFilesException
import ac.uk.ebi.biostd.submission.model.UserSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.allFiles
import ebi.ac.uk.util.collections.ifNotEmpty

class FilesValidator {
    fun validate(submission: ExtendedSubmission, filesSource: UserSource) {
        validateFiles(submission, filesSource)
    }

    private fun validateFiles(submission: ExtendedSubmission, filesSource: UserSource) {
        submission.allFiles()
            .filter { file -> filesSource.exists(file.path).not() }
            .ifNotEmpty { throw InvalidFilesException(it, INVALID_FILES_ERROR_MSG) }
    }
}
