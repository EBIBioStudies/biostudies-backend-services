package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.filesystem.extensions.persistFireFile
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

        val section = process(sub.section) { updateFileList(sub.accNo, it, sub.relPath, fileListFiles) }

        return when {
            section.changed -> sub.copy(
                pageTabFiles = subExtFiles(sub.accNo, subFiles, sub.relPath),
                section = section.section
            )
            else -> sub.copy(pageTabFiles = subExtFiles(sub.accNo, subFiles, sub.relPath))
        }
    }

    private fun updateFileList(
        accNo: String,
        sec: ExtSection,
        path: String,
        pageTabFiles: Map<String, PageTabFiles>
    ): Section = when (val lst = sec.fileList) {
        null -> Section(false, sec)
        else -> {
            val name = lst.filePath
            val files = pageTabFiles.getValue(name)
            Section(true, sec.copy(fileList = lst.copy(pageTabFiles = fileListFiles(accNo, files, path, name))))
        }
    }

    private fun fileListFiles(accNo: String, pageTab: PageTabFiles, subFolder: String, fileListName: String) = listOf(
        saveFileListFile(accNo, pageTab.json, subFolder, "$fileListName.json"),
        saveFileListFile(accNo, pageTab.xml, subFolder, "$fileListName.xml"),
        saveFileListFile(accNo, pageTab.tsv, subFolder, "$fileListName.pagetab.tsv")
    )

    private fun saveFileListFile(accNo: String, file: File, subFolder: String, filePath: String): FireFile {
        val relPath = "Files/$filePath"
        val fireFile = fireWebClient.persistFireFile(accNo, file, file.md5(), "$subFolder/$relPath")

        return FireFile(filePath, relPath, fireFile.fireOid, fireFile.objectMd5, fireFile.objectSize.toLong(), listOf())
    }

    private fun subExtFiles(accNo: String, pageTab: PageTabFiles, subFolder: String): List<ExtFile> = listOf(
        saveSubFile(accNo, pageTab.json, subFolder),
        saveSubFile(accNo, pageTab.xml, subFolder),
        saveSubFile(accNo, pageTab.tsv, subFolder)
    )

    private fun saveSubFile(accNo: String, file: File, subFolder: String): FireFile {
        val name = file.name
        val fireFile = fireWebClient.persistFireFile(accNo, file, file.md5(), "$subFolder/$name")

        return FireFile(name, name, fireFile.fireOid, fireFile.objectMd5, fireFile.objectSize.toLong(), listOf())
    }
}
