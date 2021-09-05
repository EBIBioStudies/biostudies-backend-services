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

        val tabs = serializationService.generatePageTab2(submission, fireTempFolder, fireTempFolder.resolve(FILES_PATH))
            .let {
                SubmissionSavedPageTab(
                    it.submissionPageTab.saveSubmissionTabFile(fireTempFolder.absolutePath),
                    it.fileListPageTabs.saveFileList(fireTempFolder.absolutePath)
                )
            }

        logger.info { "page tab successfully generated for submission ${submission.accNo}" }
        return submission.copy(pageTabFiles = tabs.submissionTabFiles.toFireFiles())
    }

    private fun List<TabFiles>.saveFileList(
        fireFolderPath: String
    ): Map<String, WebFireFile> {
        return associate {
            it.json.name to fireWebClient.save(it.json, it.json.md5(), fireFolderPath)
            it.xml.name to fireWebClient.save(it.xml, it.xml.md5(), fireFolderPath)
            it.tsv.name to fireWebClient.save(it.tsv, it.tsv.md5(), fireFolderPath)
        }
    }

    private fun TabFiles.saveSubmissionTabFile(fireFolderPath: String): Map<String, WebFireFile> {
        return mapOf(
            json.name to fireWebClient.save(json, json.md5(), fireFolderPath),
            xml.name to fireWebClient.save(xml, xml.md5(), fireFolderPath),
            tsv.name to fireWebClient.save(tsv, tsv.md5(), fireFolderPath)
        )
    }
}

private fun Map<String, WebFireFile>.toFireFiles(): List<FireFile> {
    return this.map { FireFile(it.key, it.value.fireOid, it.value.objectMd5, it.value.objectSize.toLong(), listOf()) }
}

data class SubmissionSavedPageTab(
    val submissionTabFiles: Map<String, WebFireFile>,
    val fileListTabFiles: Map<String, WebFireFile>
)
