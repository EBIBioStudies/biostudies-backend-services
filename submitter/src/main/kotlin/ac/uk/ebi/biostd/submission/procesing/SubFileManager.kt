package ac.uk.ebi.biostd.submission.procesing

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ebi.ac.uk.model.SubFields
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.model.allFiles
import ebi.ac.uk.paths.FolderResolver
import org.apache.commons.io.FileUtils
import java.nio.file.Path


class SubFileManager(
        private val folderResolver: FolderResolver,
        private val serializationService: SerializationService) {

    fun generateSubFiles(submission: Submission, user: User) {
        generateOutputFiles(submission)
        copyFiles(submission, user)
    }

    private fun generateOutputFiles(submission: Submission) {
        val json = serializationService.serializeSubmission(submission, SubFormat.JSON)
        val xml = serializationService.serializeSubmission(submission, SubFormat.XML)
        val tsv = serializationService.serializeSubmission(submission, SubFormat.TSV)

        val accNo: String = submission[SubFields.ACC_NO]
        val submissionPath = folderResolver.getSubmissionFolder(submission)
        FileUtils.writeStringToFile(submissionPath.resolve("$accNo.json").toFile(), json, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$accNo.xml").toFile(), xml, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$accNo.tsv").toFile(), tsv, Charsets.UTF_8)
    }

    private fun copyFiles(submission: Submission, user: User) {
        val userPath = folderResolver.getUserMagicFolderPath(user.id, user.secretKey)

        submission.allFiles().forEach { file ->
            val sourceFile = getFilePath(userPath, submission[SubFields.ROOT_PATH], file.name).toFile()
            val submissionFile = folderResolver.getSubFilePath(submission[SubFields.REL_PATH], file.name).toFile()
            FileUtils.copyFile(sourceFile, submissionFile)
        }
    }

    private fun getFilePath(basePath: Path, rootPath: String?, file: String) = basePath.resolve(rootPath).resolve(file)
}
