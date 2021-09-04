package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import uk.ac.ebi.fire.client.model.FireFile as WebFireFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.paths.FILES_PATH
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import java.io.File

private val logger = KotlinLogging.logger {}

class FirePageTabService(
    private val fireTempFolder: File,
    private val serializationService: SerializationService,
    private val fireWebClient: FireWebClient
) : PageTabService {
    override fun generatePageTab(submission: ExtSubmission): ExtSubmission {
        logger.info { "generating submission ${submission.accNo} pagetab files" }

        val tabs = serializationService.generatePageTab(submission, fireTempFolder, fireTempFolder.resolve(FILES_PATH))
            .flatMap { saveTabFiles(it, fireTempFolder.absolutePath) }

        logger.info { "page tab successfully generated for submission ${submission.accNo}" }
        return submission.copy(pageTabFiles = tabs.map { it.toFireFile() })
    }

    private fun saveTabFiles(tabFiles: TabFiles, fireFolderPath: String): List<Pair<String, WebFireFile>> {
        return listOf(
            Pair(tabFiles.json.name, fireWebClient.save(tabFiles.json, tabFiles.json.md5(), fireFolderPath)),
            Pair(tabFiles.xml.name, fireWebClient.save(tabFiles.xml, tabFiles.xml.md5(), fireFolderPath)),
            Pair(tabFiles.tsv.name, fireWebClient.save(tabFiles.tsv, tabFiles.tsv.md5(), fireFolderPath))
        )
    }
}

private fun Pair<String, WebFireFile>.toFireFile(): FireFile {
    return FireFile(first, second.fireOid, second.objectMd5, second.objectSize.toLong(), listOf())
}
