package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.biostd.submission.model.FilesSource
import ac.uk.ebi.biostd.submission.model.ResourceFile
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.paths.FolderResolver

const val INVALID_FILES_ERROR_MSG = "Submission contains invalid files"

class FilesHandler(
    private val folderResolver: FolderResolver,
    private val filesValidator: FilesValidator,
    private val filesCopier: FilesCopier,
    private val libraryFilesHandler: LibraryFilesHandler,
    private val outputFilesGenerator: OutputFilesGenerator
) {
    /**
     * In charge of generate submission json/tsv/xml representation files and validate all submission specified files
     * are provided or exists in  user repository or in the list of provided files.
     */
    fun processFiles(submission: ExtendedSubmission, files: List<ResourceFile>, format: SubFormat) {
        val userFolder = folderResolver.getUserMagicFolderPath(submission.user.id, submission.user.secretKey)
        val fileSource = FilesSource(files, userFolder)

        filesValidator.validate(submission, fileSource)
        libraryFilesHandler.processLibraryFiles(submission, fileSource, format)
        filesCopier.copy(submission, fileSource)
        outputFilesGenerator.generate(submission)
    }
}
