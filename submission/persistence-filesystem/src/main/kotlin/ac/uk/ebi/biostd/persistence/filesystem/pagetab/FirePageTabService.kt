package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.filePermissions
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.folderPermissions
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import java.io.File

private val logger = KotlinLogging.logger {}

class FirePageTabService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService,
    private val fireWebClient: FireWebClient
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
        ).forEach { fireWebClient.save(it, it.md5(), submissionFolder.absolutePath) }

        submission.allFileList.flatMap {
            pageTab(
                it.toFilesTable(),
                filesFolder,
                it.fileName,
                filePermissions,
                folderPermissions,
                serializationService
            )
        }.forEach { fireWebClient.save(it, it.md5(), submissionFolder.absolutePath) }

        logger.info { "page tab successfully generated for submission $accNo" }
    }
}
