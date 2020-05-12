package ac.uk.ebi.biostd.persistence.service.filesystem

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.integration.FileMode
import ebi.ac.uk.extended.mapping.serialization.to.toFilesTable
import ebi.ac.uk.extended.mapping.serialization.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.extended.model.allFiles
import ebi.ac.uk.extended.model.allReferencedFiles
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.FileUtils.copyOrReplaceFile
import ebi.ac.uk.io.FileUtils.deleteFolder
import ebi.ac.uk.io.FileUtils.moveFile
import ebi.ac.uk.io.FileUtils.reCreateDirectory
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import java.io.File
import java.nio.file.Path

class FilesService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) {
    fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode) {
        val submissionPath = folderResolver.getSubmissionFolder(submission.relPath)
        generateOutputFiles(submission, submissionPath)
        when (mode) {
            FileMode.MOVE -> processFiles(submission, submissionPath, ::move)
            FileMode.COPY -> processFiles(submission, submissionPath, ::copy)
        }
    }

    private fun generateOutputFiles(submission: ExtSubmission, submissionPath: Path) {
        val simpleSubmission = submission.toSimpleSubmission()

        generateOutputFiles(simpleSubmission, submissionPath, submission.accNo)
        submission.allFileList.forEach { generateOutputFiles(it.toFilesTable(), submissionPath, it.fileName) }
    }

    // TODO add file list content validation to integration tests
    // TODO add special character folders/files names test
    // TODO Test temporally folder already existing
    // TODO we need to remove also pagetab files as only FILES are clean right now
    // TODO add integration test for file list within subsections
    private fun <T> generateOutputFiles(element: T, submissionPath: Path, outputFileName: String) {
        val json = serializationService.serializeElement(element, SubFormat.JSON_PRETTY)
        val xml = serializationService.serializeElement(element, SubFormat.XML)
        val tsv = serializationService.serializeElement(element, SubFormat.TSV)

        FileUtils.copyOrReplace(submissionPath.resolve("$outputFileName.json").toFile(), json)
        FileUtils.copyOrReplace(submissionPath.resolve("$outputFileName.xml").toFile(), xml)
        FileUtils.copyOrReplace(submissionPath.resolve("$outputFileName.pagetab.tsv").toFile(), tsv)
    }

    private fun processFiles(submission: ExtSubmission, submissionPath: Path, process: (ExtFile, File) -> Unit) {
        val submissionFolder = getSubmissionFolder(submissionPath)
        val temporally = createTempFolder(submissionFolder, submission.accNo)

        submission.allFiles.forEach { process(it, temporally) }
        submission.allReferencedFiles.forEach { process(it, temporally) }

        val filesPath = submissionFolder.resolve(FILES_PATH)
        deleteFolder(filesPath)
        moveFile(temporally, filesPath)
    }

    private fun copy(extFile: ExtFile, file: File) = copyOrReplaceFile(extFile.file, file.resolve(extFile.fileName))
    private fun move(file: ExtFile, path: File) = moveFile(file.file, path.resolve(file.fileName))

    private fun getSubmissionFolder(submissionPath: Path): File {
        val submissionFolder = submissionPath.toFile()
        submissionFolder.mkdirs()
        return submissionFolder
    }

    private fun createTempFolder(submissionFolder: File, accNo: String): File =
        reCreateDirectory(submissionFolder.parentFile.resolve("${accNo}_temp"))
}
