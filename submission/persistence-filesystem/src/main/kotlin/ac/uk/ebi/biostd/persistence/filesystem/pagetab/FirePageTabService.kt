package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.filesystem.service.Section
import ac.uk.ebi.biostd.persistence.filesystem.service.process
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.io.ext.md5
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import java.io.File

class FirePageTabService(
    private val fireTempFolder: File,
    private val serializationService: SerializationService,
    private val fireWebClient: FireWebClient
) : PageTabService {
    override fun generatePageTab(sub: ExtSubmission): ExtSubmission {
        val (subFiles, fileListFiles) = serializationService.generatePageTab(sub, fireTempFolder)
        val section = process(sub.section) { updateFileList(it, sub.relPath, fileListFiles) }

        return when {
            section.changed -> sub.copy(tabFiles = extFiles(sub.relPath, subFiles), section = section.section)
            else -> sub.copy(tabFiles = extFiles(sub.relPath, subFiles))
        }
    }

    private fun updateFileList(sec: ExtSection, path: String, tab: Map<String, TabFiles>): Section {
        return when (val lst = sec.fileList) {
            null -> Section(false, sec)
            else -> Section(true, sec.copy(fileList = lst.copy(tabFiles = extFiles(path, tab.getValue(lst.fileName)))))
        }
    }

    private fun extFiles(filesRelPath: String, pageTab: TabFiles): List<ExtFile> {
        val json = fireWebClient.save(pageTab.json, pageTab.json.md5(), filesRelPath)
        val xml = fireWebClient.save(pageTab.xml, pageTab.xml.md5(), filesRelPath)
        val tsv = fireWebClient.save(pageTab.tsv, pageTab.tsv.md5(), filesRelPath)

        val extJson = FireFile(pageTab.json.name, json.fireOid, json.objectMd5, json.objectSize.toLong(), listOf())
        val extXml = FireFile(pageTab.xml.name, xml.fireOid, xml.objectMd5, xml.objectSize.toLong(), listOf())
        val extTsv = FireFile(pageTab.tsv.name, tsv.fireOid, tsv.objectMd5, tsv.objectSize.toLong(), listOf())

        return listOf(extJson, extXml, extTsv)
    }
}

