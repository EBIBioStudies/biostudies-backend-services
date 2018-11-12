package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constans.SubFields
import ebi.ac.uk.model.extensions.allFiles
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.paths.FolderResolver
import org.apache.commons.io.FileUtils
import java.nio.file.Path


class FilesProcessor(
        private val folderResolver: FolderResolver,
        private val serializationService: SerializationService) : SubmissionProcessor {

    override fun process(user: User, submission: ExtendedSubmission, persistenceContext: PersistenceContext) {
        generateOutputFiles(submission)
        copyFiles(submission, user)
    }

    private fun generateOutputFiles(submission: ExtendedSubmission) {
        val json = serializationService.serializeSubmission(submission, SubFormat.JSON)
        val xml = serializationService.serializeSubmission(submission, SubFormat.XML)
        val tsv = serializationService.serializeSubmission(submission, SubFormat.TSV)

        val accNo: String = submission.accNo
        val submissionPath = folderResolver.getSubmissionFolder(submission)
        FileUtils.writeStringToFile(submissionPath.resolve("$accNo.json").toFile(), json, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$accNo.xml").toFile(), xml, Charsets.UTF_8)
        FileUtils.writeStringToFile(submissionPath.resolve("$accNo.tsv").toFile(), tsv, Charsets.UTF_8)
    }

    private fun copyFiles(submission: ExtendedSubmission, user: User) {
        val userPath = folderResolver.getUserMagicFolderPath(user.id, user.secretKey)

        submission.allFiles().forEach { file ->
            val sourceFile = getFilePath(userPath, submission.rootPath, file.name).toFile()
            val submissionFile = folderResolver.getSubFilePath(submission.relPath, file.name).toFile()
            FileUtils.copyFile(sourceFile, submissionFile)
        }
    }

    private fun getFilePath(basePath: Path, rootPath: String?, file: String) = basePath.resolve(rootPath).resolve(file)
}
