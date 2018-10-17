package ac.uk.ebi.biostd.submission.procesing

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ebi.ac.uk.model.ISubmission
import ebi.ac.uk.paths.FolderResolver
import org.apache.commons.io.FileUtils
import java.nio.file.Path


class SubFileManager(
        private val folderResolver: FolderResolver,
        private val serializationService: SerializationService) {

    fun generateSubFiles(submission: ISubmission) {
        generateOutputFiles(submission)
        copyFiles(submission)
    }

    private fun generateOutputFiles(submission: ISubmission) {
        val json = serializationService.serializeSubmission(submission, SubFormat.JSON)
        val xml = serializationService.serializeSubmission(submission, SubFormat.XML)
        val tsv = serializationService.serializeSubmission(submission, SubFormat.TSV)

        val accNo = submission.accNo
        val submissionPath = folderResolver.getSubmissionFolder(submission)
        FileUtils.writeStringToFile(submissionPath.resolve("$accNo.json").toFile(), json, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$accNo.xml").toFile(), xml, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$accNo.tsv").toFile(), tsv, Charsets.UTF_8)
    }

    private fun copyFiles(submission: ISubmission) {
        val userPath = folderResolver.getUserMagicFolderPath(submission.user.id, submission.user.secretKey)

        submission.allFiles.forEach { file ->
            val sourceFile = getFilePath(userPath, submission.rootPath, file.name).toFile()
            val submissionFile = folderResolver.getSubFilePath(submission.relPath, file.name).toFile()
            FileUtils.copyFile(sourceFile, submissionFile)
        }
    }

    private fun getFilePath(basePath: Path, rootPath: String?, file: String) = basePath.resolve(rootPath).resolve(file)
}
