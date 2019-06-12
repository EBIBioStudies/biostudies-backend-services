package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.submission.model.UserSource
import ebi.ac.uk.model.ExtendedSubmission

const val INVALID_FILES_ERROR_MSG = "Submission contains invalid files"

class FilesHandler(
    private val filesValidator: FilesValidator,
    private val filesCopier: FilesCopier,
    private val outputFilesGenerator: OutputFilesGenerator
) {
    /**
     * In charge of generate submission json/tsv/xml representation files and validate all submission specified files
     * are provided or exists in  user repository or in the list of provided files.
     */
    fun processFiles(submission: ExtendedSubmission, fileSource: UserSource) {
        filesValidator.validate(submission, fileSource)
        filesCopier.copy(submission, fileSource)
        outputFilesGenerator.generate(submission)
    }
}
