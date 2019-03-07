package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat.JSON_PRETTY
import ac.uk.ebi.biostd.SubFormat.TSV
import ac.uk.ebi.biostd.SubFormat.XML
import ac.uk.ebi.biostd.submission.exceptions.InvalidFilesException
import ac.uk.ebi.biostd.submission.exceptions.InvalidLibraryFileException
import ac.uk.ebi.biostd.submission.model.FilesSource
import ac.uk.ebi.biostd.submission.model.ListFilesSource
import ac.uk.ebi.biostd.submission.model.PathFilesSource
import ac.uk.ebi.biostd.submission.model.ResourceFile
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.extensions.allFiles
import ebi.ac.uk.model.extensions.allReferencedFiles
import ebi.ac.uk.model.extensions.libFileSections
import ebi.ac.uk.paths.FolderResolver
import ebi.ac.uk.util.collections.ifNotEmpty
import org.apache.commons.io.FileUtils

const val INVALID_FILES_ERROR_MSG = "Submission contains invalid files"

class FilesHandler(private val folderResolver: FolderResolver, private val serializationService: SerializationService) {

    /**
     * In charge of generate submission json/tsv/xml representation files and validate all submission specified files
     * are provided or exists in  user repository or in the list of provided files.
     */
    fun processFiles(submission: ExtendedSubmission, files: List<ResourceFile>) {
        val fileSource = if (files.isEmpty()) PathFilesSource(getUserFolder(submission)) else ListFilesSource(files)

        validateFiles(submission, fileSource)
        copyFiles(submission.allFiles(), submission, fileSource)

        validateLibraryFiles(submission, fileSource)
        processLibraryFiles(submission, fileSource)
        copyFiles(submission.allReferencedFiles(), submission, fileSource)

        generateOutputFiles(submission.asSubmission(), submission, submission.accNo)
    }

    private fun <T> generateOutputFiles(element: T, submission: ExtendedSubmission, outputFileName: String) {
        val json = serializationService.serializeElement(element, JSON_PRETTY)
        val xml = serializationService.serializeElement(element, XML)
        val tsv = serializationService.serializeElement(element, TSV)
        val submissionPath = folderResolver.getSubmissionFolder(submission)

        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.json").toFile(), json, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.xml").toFile(), xml, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$outputFileName.tsv").toFile(), tsv, Charsets.UTF_8)
    }

    private fun validateLibraryFiles(submission: ExtendedSubmission, filesSource: FilesSource) {
        submission.libFileSections()
            .filterNot { section -> filesSource.exists(section.libraryFile!!.name) }
            .ifNotEmpty { throw InvalidLibraryFileException(it) }
    }

    private fun processLibraryFiles(submission: ExtendedSubmission, filesSource: FilesSource) {
        submission.libFileSections().forEach { section ->
            val libFileName = "${submission.accNo}.${section.accNo}.files"
            val libFileContent = filesSource.readText(section.libraryFile!!.name)
            val filesTable = serializationService.deserializeElement<FilesTable>(libFileContent, TSV)

            filesTable.elements.forEach { file -> section.addReferencedFile(file) }
            generateOutputFiles(filesTable, submission, libFileName)
            section.libraryFile?.name = "$libFileName.tsv"
        }
    }

    private fun validateFiles(submission: ExtendedSubmission, filesSource: FilesSource) {
        submission.allFiles()
            .filter { file -> filesSource.exists(file.path).not() }
            .ifNotEmpty { throw InvalidFilesException(it, INVALID_FILES_ERROR_MSG) }
    }

    private fun copyFiles(files: List<File>, submission: ExtendedSubmission, filesSource: FilesSource) {
        files.forEach { file ->
            val submissionFile = folderResolver.getSubFilePath(submission.relPath, file.path).toFile()

            file.size = filesSource.size(file.path)
            FileUtils.copyInputStreamToFile(filesSource.getInputStream(file.path), submissionFile)
        }
    }

    private fun getUserFolder(submission: ExtendedSubmission) =
        folderResolver.getUserMagicFolderPath(submission.user.id, submission.user.secretKey)
}
