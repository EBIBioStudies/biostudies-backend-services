package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.paths.FILES_PATH
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import java.io.File

private val logger = KotlinLogging.logger {}

class FirePageTabService(
    private val tempFireFolder: File,
    private val serializationService: SerializationService,
    private val fireWebClient: FireWebClient
) : PageTabService {
    override fun generatePageTab(submission: ExtSubmission) {
        logger.info { "generating submission ${submission.accNo} pagetab files" }

        serializationService.generatePageTab(submission, tempFireFolder, tempFireFolder.resolve(FILES_PATH))
            .forEach { saveTabFiles(it, tempFireFolder.absolutePath) }

        logger.info { "page tab successfully generated for submission ${submission.accNo}" }
    }

    private fun saveTabFiles(tabFiles: TabFiles, relPath: String) {
        fireWebClient.save(tabFiles.json, tabFiles.json.md5(), relPath)
        fireWebClient.save(tabFiles.xml, tabFiles.xml.md5(), relPath)
        fireWebClient.save(tabFiles.tsv, tabFiles.tsv.md5(), relPath)
    }
}
