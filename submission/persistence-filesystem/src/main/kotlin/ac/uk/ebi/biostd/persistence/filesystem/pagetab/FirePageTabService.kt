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
                true,
                sec.copy(
                    fileList = lst.copy(
                        pageTabFiles = extFiles(
                            tab.getValue(lst.fileName),
                            path,
                            "Files",
                            lst.fileName
                        )
                    )
                )
            )
        }
    }

    private fun extFiles(
        pageTab: TabFiles,
        subFolder: String,
        relPath: String? = null,
        fileListName: String? = null
    ): List<ExtFile> = listOf(
        saveFile(pageTab.json, subFolder, relPath, fileListName),
        saveFile(pageTab.xml, subFolder, relPath, fileListName),
        saveFile(pageTab.tsv, subFolder, relPath, fileListName)
    )

    private fun saveFile(
        file: File,
        subFolder: String,
        partialRelPath: String? = null,
        fileList: String? = null
    ): FireFile {
        val filePath =
            if (fileList != null && fileList.contains("/")) "${fileList.substringBeforeLast("/")}/${file.name}"
            else file.name
        val relPath = if (partialRelPath != null) "${partialRelPath}/$filePath" else filePath
        val db = fireWebClient.save(file, file.md5(), "$subFolder/${relPath}")
        return FireFile(
            fileName = filePath.substringAfterLast("/"),
            filePath = filePath,
            relPath = relPath,
            fireId = db.fireOid,
            md5 = db.objectMd5,
            size = db.objectSize.toLong(),
            attributes = listOf()
        )
    }
}
