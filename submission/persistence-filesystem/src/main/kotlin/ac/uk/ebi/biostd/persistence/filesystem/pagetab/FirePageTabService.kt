package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileProcessingService
import ac.uk.ebi.biostd.persistence.filesystem.service.Section
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.io.ext.md5
import java.io.File
import uk.ac.ebi.fire.client.integration.web.FireWebClient

class FirePageTabService(
    private val fireTempFolder: File,
    private val serializationService: SerializationService,
    private val fireWebClient: FireWebClient,
    private val pageTabUtil: PageTabUtil,
    private val fileProcessingService: FileProcessingService
) : PageTabService {
    override fun generatePageTab(sub: ExtSubmission): ExtSubmission {
        val subFiles = pageTabUtil.generateSubPageTab(serializationService, sub, fireTempFolder)
        val fileListFiles = pageTabUtil.generateFileListPageTab(serializationService, sub, fireTempFolder)

        val section = fileProcessingService.process(sub.section) { updateFileList(it, sub.relPath, fileListFiles) }

        return when {
            section.changed -> sub.copy(pageTabFiles = subExtFiles(subFiles, sub.relPath), section = section.section)
            else -> sub.copy(pageTabFiles = subExtFiles(subFiles, sub.relPath))
        }
    }

    private fun updateFileList(sec: ExtSection, path: String, pageTabFiles: Map<String, PageTabFiles>): Section {
        return when (val lst = sec.fileList) {
            null -> Section(false, sec)
            else -> {
                val name = lst.filePath
                val files = pageTabFiles.getValue(name)
                Section(true, sec.copy(fileList = lst.copy(pageTabFiles = fileListFiles(files, path, name))))
            }
        }
    }

    private fun fileListFiles(pageTab: PageTabFiles, subFolder: String, fileListName: String) = listOf(
        saveFileListFile(pageTab.json, subFolder, "$fileListName.json"),
        saveFileListFile(pageTab.xml, subFolder, "$fileListName.xml"),
        saveFileListFile(pageTab.tsv, subFolder, "$fileListName.pagetab.tsv")
    )

    private fun saveFileListFile(file: File, subFolder: String, filePath: String): FireFile {
        val relPath = "Files/$filePath"
        val db = fireWebClient.save(file, file.md5())
        fireWebClient.setPath(db.fireOid, "$subFolder/$relPath")

        return FireFile(filePath, relPath, db.fireOid, db.objectMd5, db.objectSize.toLong(), listOf())
    }

    private fun subExtFiles(pageTab: PageTabFiles, subFolder: String): List<ExtFile> = listOf(
        saveSubFile(pageTab.json, subFolder),
        saveSubFile(pageTab.xml, subFolder),
        saveSubFile(pageTab.tsv, subFolder)
    )

    private fun saveSubFile(file: File, subFolder: String): FireFile {
        val name = file.name
        val db = fireWebClient.save(file, file.md5())
        fireWebClient.setPath(db.fireOid, "$subFolder/$name")

        return FireFile(name, name, db.fireOid, db.objectMd5, db.objectSize.toLong(), listOf())
    }
}
