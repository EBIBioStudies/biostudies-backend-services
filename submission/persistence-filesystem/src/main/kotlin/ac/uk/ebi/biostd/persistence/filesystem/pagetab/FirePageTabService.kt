package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.io.ext.md5
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import java.io.File

private val logger = KotlinLogging.logger {}

class FirePageTabService(
    private val fireTempFolder: File,
    private val serializationService: SerializationService,
    private val fireWebClient: FireWebClient
) : PageTabService {
    override fun generatePageTab(sub: ExtSubmission): ExtSubmission {
        logger.info { "generating submission ${sub.accNo} pagetab files" }
        val tabFiles = serializationService.generatePageTab(sub, fireTempFolder)

        logger.info { "page tab successfully generated for submission ${sub.accNo}" }

        val config = SavePageTabConfig(tabFiles.fileListTabFiles, sub.relPath)

        return sub.copy(
            pageTabFiles = asExtFiles(sub.relPath, tabFiles.subTabFiles),
            section = savePageTabFiles(sub.section, tabFiles.fileListTabFiles, sub.relPath)
        )
    }

    private fun savePageTabFiles(
        section: ExtSection,
        fileListTabFiles: Map<String, TabFiles>,
        relPath: String
    ): ExtSection {
        return if (section.fileList != null) {
            section.copy(
                fileList = savePageTabFileList(section.fileList!!, fileListTabFiles, relPath),
                sections = section.sections.map { savePageTabSections(it, fileListTabFiles, relPath) })
        } else section.copy(sections = section.sections.map { savePageTabSections(it, fileListTabFiles, relPath) })
    }

    private fun savePageTabFileList(
        fileList: ExtFileList,
        fileListTabFiles: Map<String, TabFiles>,
        relPath: String
    ): ExtFileList =
        fileList.copy(pageTabFiles = asExtFiles(relPath, requireNotNull(fileListTabFiles[fileList.fileName])))

    private fun savePageTabSections(
        subSection: Either<ExtSection, ExtSectionTable>,
        fileListTabFiles: Map<String, TabFiles>,
        relPath: String
    ): Either<ExtSection, ExtSectionTable> {
        return subSection.bimap(
            { extSection -> savePageTabFiles(extSection, fileListTabFiles, relPath) },
            { it.copy(sections = it.sections.map { sec -> savePageTabFiles(sec, fileListTabFiles, relPath) }) }
        )
    }

    private fun asExtFiles(filesRelPath: String, pageTab: TabFiles): List<ExtFile> {
        val json = fireWebClient.save(pageTab.json, pageTab.json.md5(), filesRelPath)
        val xml = fireWebClient.save(pageTab.xml, pageTab.xml.md5(), filesRelPath)
        val tsv = fireWebClient.save(pageTab.tsv, pageTab.tsv.md5(), filesRelPath)

        val extJson = FireFile(pageTab.json.name, json.fireOid, json.objectMd5, json.objectSize.toLong(), listOf())
        val extXml = FireFile(pageTab.xml.name, xml.fireOid, xml.objectMd5, xml.objectSize.toLong(), listOf())
        val extTsv = FireFile(pageTab.tsv.name, tsv.fireOid, tsv.objectMd5, tsv.objectSize.toLong(), listOf())

        return listOf(extJson, extXml, extTsv)
    }
}

data class SavePageTabConfig(
    val fileListTabFiles: Map<String, TabFiles>,
    val relPath: String
)
