package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class NfsPageTabService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) : PageTabService {
    override fun generatePageTab(submission: ExtSubmission) {
        val submissionFolder = folderResolver.getSubFolder(submission.relPath).toFile()
        val filesFolder = submissionFolder.resolve(FILES_PATH)
        logger.info { "generating submission ${submission.accNo} pagetab files" }

        serializationService.generatePageTab(submission, submissionFolder, filesFolder)

        logger.info { "page tab successfully generated for submission ${submission.accNo}" }
    }
}
