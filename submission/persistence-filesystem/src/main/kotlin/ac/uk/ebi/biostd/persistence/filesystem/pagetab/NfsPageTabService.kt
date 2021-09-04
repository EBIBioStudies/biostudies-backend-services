package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class NfsPageTabService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) : PageTabService {
    override fun generatePageTab(submission: ExtSubmission): ExtSubmission {
        val submissionFolder = folderResolver.getSubFolder(submission.relPath).toFile()
        val filesFolder = submissionFolder.resolve(FILES_PATH)
        logger.info { "generating submission ${submission.accNo} pagetab files" }

        val tabs = serializationService.generatePageTab(submission, submissionFolder, filesFolder)

        logger.info { "page tab successfully generated for submission ${submission.accNo}" }
        return submission.copy(pageTabFiles = tabs.flatMap { it.toNfsFile() })
    }
}

private fun TabFiles.toNfsFile(): List<NfsFile> {
    return listOf(NfsFile(json.name, json), NfsFile(xml.name, xml), NfsFile(tsv.name, tsv))
}
