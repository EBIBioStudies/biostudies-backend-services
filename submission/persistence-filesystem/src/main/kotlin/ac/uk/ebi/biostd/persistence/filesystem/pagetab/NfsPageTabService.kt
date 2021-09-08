package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class NfsPageTabService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) : PageTabService {
    override fun generatePageTab(sub: ExtSubmission): ExtSubmission {
        val submissionFolder = folderResolver.getSubFolder(sub.relPath).toFile()
        logger.info { "generating submission ${sub.accNo} pagetab files" }

        val tabsFiles = serializationService.generatePageTab(sub, submissionFolder)

        logger.info { "page tab successfully generated for submission ${sub.accNo}" }
        return sub.copy(tabFiles = toNfsFiles(tabsFiles.subFiles))
    }

    private fun toNfsFiles(tabsFiles: TabFiles): List<NfsFile> {
        return listOf(
            NfsFile(tabsFiles.json.name, tabsFiles.json),
            NfsFile(tabsFiles.xml.name, tabsFiles.xml),
            NfsFile(tabsFiles.tsv.name, tabsFiles.tsv))
    }
}
