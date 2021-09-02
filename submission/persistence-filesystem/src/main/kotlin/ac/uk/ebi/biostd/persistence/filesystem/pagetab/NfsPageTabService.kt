package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.filePermissions
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.folderPermissions
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class NfsPageTabService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) : PageTabService {
    override fun generatePageTab(submission: ExtSubmission) {
        val accNo = submission.accNo
        val filePermissions = submission.filePermissions()
        val folderPermissions = submission.folderPermissions()
        val submissionFolder = folderResolver.getSubFolder(submission.relPath).toFile()
        val filesFolder = submissionFolder.resolve(FILES_PATH)
        logger.info { "generating submission $accNo pagetab files" }

        pageTab(
            submission.toSimpleSubmission(),
            submissionFolder,
            accNo,
            filePermissions,
            folderPermissions,
            serializationService
        )
        submission.allFileList.forEach {
            pageTab(
                it.toFilesTable(),
                filesFolder,
                it.fileName,
                filePermissions,
                folderPermissions,
                serializationService
            )
        }

        logger.info { "page tab successfully generated for submission $accNo" }
    }
}
