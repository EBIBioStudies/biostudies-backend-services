package ac.uk.ebi.biostd.persistence.service

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
import ebi.ac.uk.io.NfsFileUtils
import ebi.ac.uk.io.NfsFileUtils.copyOrReplaceFile
import ebi.ac.uk.io.NfsFileUtils.deleteFolder
import ebi.ac.uk.io.NfsFileUtils.moveFile
import ebi.ac.uk.io.NfsFileUtils.reCreateDirectory
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import java.io.File

class FilePersistenceService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) {
    fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode) {
        generateOutputFiles(submission)
        when (mode) {
            FileMode.MOVE -> processFiles(submission, ::move)
            FileMode.COPY -> processFiles(submission, ::copy)
        }
    }

    private fun generateOutputFiles(submission: ExtSubmission) {
        val simpleSubmission = submission.toSimpleSubmission()

        generateOutputFiles(simpleSubmission, submission.relPath, submission.accNo)
        submission.allFileList.forEach { generateOutputFiles(it.toFilesTable(), submission.relPath, it.fileName) }
    }

    // TODO add file list content validation to integration tests
    // TODO add special character folders/files names test
    // TODO Test temporally folder already existing
    // TODO we need to remove also pagetab files as only FILES are clean right now
    // TODO add integration test for file list within subsections
    private fun <T> generateOutputFiles(element: T, relPath: String, outputFileName: String) {
        val json = serializationService.serializeElement(element, SubFormat.JSON_PRETTY)
        val xml = serializationService.serializeElement(element, SubFormat.XML)
        val tsv = serializationService.serializeElement(element, SubFormat.TSV)
        val submissionPath = folderResolver.getSubmissionFolder(relPath)

        NfsFileUtils.copyOrReplace(submissionPath.resolve("$outputFileName.json").toFile(), json)
        NfsFileUtils.copyOrReplace(submissionPath.resolve("$outputFileName.xml").toFile(), xml)
        NfsFileUtils.copyOrReplace(submissionPath.resolve("$outputFileName.pagetab.tsv").toFile(), tsv)
    }

    private fun processFiles(submission: ExtSubmission, process: (ExtFile, File) -> Unit) {
        val submissionFolder = getSubmissionFolder(submission.relPath)
        val temporally = createTempFolder(submissionFolder, submission.accNo)

        submission.allFiles.forEach { process(it, temporally) }
        submission.allReferencedFiles.forEach { process(it, temporally) }

        val filesPath = submissionFolder.resolve(FILES_PATH)
        deleteFolder(filesPath)
        moveFile(temporally, filesPath)
    }

    private fun copy(extFile: ExtFile, file: File) = copyOrReplaceFile(extFile.file, file.resolve(extFile.fileName))
    private fun move(file: ExtFile, path: File) = moveFile(file.file, path.resolve(file.fileName))

    private fun getSubmissionFolder(relPath: String): File {
        val submissionFolder = folderResolver.getSubmissionFolder(relPath).toFile()
        submissionFolder.mkdirs()
        return submissionFolder
    }

    private fun createTempFolder(submissionFolder: File, accNo: String): File =
        reCreateDirectory(submissionFolder.parentFile.resolve("${accNo}_temp"))
}
