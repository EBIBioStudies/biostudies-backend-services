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
            section.changed -> sub.copy(pageTabFiles = subExtFiles(subFiles, sub.relPath), section = section.section)
            else -> sub.copy(pageTabFiles = subExtFiles(subFiles, sub.relPath))
        }
    }

    private fun updateFileList(sec: ExtSection, path: String, tab: Map<String, TabFiles>): Section {
        return when (val lst = sec.fileList) {
            null -> Section(false, sec)
            else -> {
                val name = lst.fileName
                val tabFiles = tab.getValue(name)
                Section(true, sec.copy(fileList = lst.copy(pageTabFiles = fileLstFiles(tabFiles, path, name))))
            }
        }
    }

    private fun fileLstFiles(pageTab: TabFiles, subFolder: String, fileListName: String) = listOf(
        saveFileListFile(pageTab.json, subFolder, "$fileListName.json"),
        saveFileListFile(pageTab.xml, subFolder, "$fileListName.xml"),
        saveFileListFile(pageTab.tsv, subFolder, "$fileListName.pagetab.tsv")
    )

    private fun saveFileListFile(file: File, subFolder: String, filePath: String): FireFile {
        val name = file.name
        val relPath = "Files/$filePath"
        val db = fireWebClient.save(file, file.md5(), "$subFolder/$relPath")
        return FireFile(
            fileName = name,
            filePath = filePath,
            relPath = relPath,
            fireId = db.fireOid,
            md5 = db.objectMd5,
            size = db.objectSize.toLong(),
            attributes = listOf()
        )
    }

    private fun subExtFiles(pageTab: TabFiles, subFolder: String): List<ExtFile> = listOf(
        saveSubFile(pageTab.json, subFolder),
        saveSubFile(pageTab.xml, subFolder),
        saveSubFile(pageTab.tsv, subFolder)
    )

    private fun saveSubFile(file: File, subFolder: String): FireFile {
        val name = file.name
        val db = fireWebClient.save(file, file.md5(), "$subFolder/${file.name}")
        return FireFile(name, name, name, db.fireOid, db.objectMd5, db.objectSize.toLong(), listOf())
    }
}
