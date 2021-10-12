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
        val subFiles = serializationService.generateSubPageTab(sub, fireTempFolder)
        val fileListFiles = serializationService.generateFileListPageTab(sub, fireTempFolder)

        val section = process(sub.section) { updateFileList(it, sub.relPath, fileListFiles) }

        return when {
            section.changed -> sub.copy(pageTabFiles = extFiles(subFiles, sub.relPath), section = section.section)
            else -> sub.copy(pageTabFiles = extFiles(subFiles, sub.relPath))
        }
    }

    private fun updateFileList(sec: ExtSection, path: String, tab: Map<String, TabFiles>): Section {
        return when (val lst = sec.fileList) {
            null -> Section(false, sec)
            else -> Section(
                true, sec.copy(fileList = lst.copy(pageTabFiles = extFiles(tab.getValue(lst.fileName), path, "Files")))
            )
        }
    }

    private fun extFiles(pageTab: TabFiles, subFolder: String, realpath: String? = null): List<ExtFile> = listOf(
        saveFile(pageTab.json, subFolder, realpath),
        saveFile(pageTab.xml, subFolder, realpath),
        saveFile(pageTab.tsv, subFolder, realpath))

    private fun saveFile(file: File, subFolder: String, relPath: String? = null): FireFile {
        val name = file.name
        val relpath = if (relPath != null) "${relPath}/$name" else name
        val db = fireWebClient.save(file, file.md5(), "$subFolder/${relpath}")
        return FireFile(
            fileName = name,
            filePath = name,
            relPath = relpath,
            fireId = db.fireOid,
            md5 = db.objectMd5,
            size = db.objectSize.toLong(),
            attributes = listOf())
    }
}
