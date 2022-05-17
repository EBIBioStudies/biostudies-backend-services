package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.common.TsvPagetabExtension
import ac.uk.ebi.biostd.persistence.filesystem.extensions.persistFireFile
import ac.uk.ebi.biostd.persistence.filesystem.service.FileProcessingService
import ac.uk.ebi.biostd.persistence.filesystem.service.Section
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.io.ext.md5
import uk.ac.ebi.fire.client.integration.web.FireOperations
import java.io.File

class FirePageTabService(
    private val fireTempFolder: File,
    private val fireOperations: FireOperations,
    private val pageTabUtil: PageTabUtil,
    private val fileProcessingService: FileProcessingService,
    private val tsvPagetabExtension: TsvPagetabExtension
) : PageTabService {
    override fun generatePageTab(sub: ExtSubmission): ExtSubmission {
        val subFiles = pageTabUtil.generateSubPageTab(sub, fireTempFolder)
        val fileListFiles = pageTabUtil.generateFileListPageTab(sub, fireTempFolder)

        val section = fileProcessingService.process(sub.section) {
            updateFileList(sub.accNo, it, sub.relPath, fileListFiles)
        }

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
    ): Section {
        return when (val lst = sec.fileList) {
            null -> Section(false, sec)
            else -> {
                val name = lst.filePath
                val files = pageTabFiles.getValue(name)
                Section(true, sec.copy(fileList = lst.copy(pageTabFiles = fileListFiles(accNo, files, path, name))))
            }
        }
    }

    private fun fileListFiles(accNo: String, pageTab: PageTabFiles, subFolder: String, fileListName: String) = listOf(
        saveFileListFile(accNo, pageTab.json, subFolder, "$fileListName.json"),
        saveFileListFile(accNo, pageTab.xml, subFolder, "$fileListName.xml"),
        saveFileListFile(accNo, pageTab.tsv, subFolder, "$fileListName.${tsvPagetabExtension.tsvExtension()}")
    )

    private fun saveFileListFile(accNo: String, file: File, subFolder: String, filePath: String): FireFile {
        val relPath = "Files/$filePath"
        val fireFile = fireOperations.persistFireFile(accNo, file, FILE, file.md5(), "$subFolder/$relPath")

        return FireFile(
            filePath,
            relPath,
            fireFile.fireOid,
            fireFile.objectMd5,
            fireFile.objectSize.toLong(),
            FILE,
            listOf()
        )
    }

    private fun subExtFiles(accNo: String, pageTab: PageTabFiles, subFolder: String): List<ExtFile> = listOf(
        saveSubFile(accNo, pageTab.json, subFolder),
        saveSubFile(accNo, pageTab.xml, subFolder),
        saveSubFile(accNo, pageTab.tsv, subFolder)
    )

    private fun saveSubFile(accNo: String, file: File, subFolder: String): FireFile {
        val fName = file.name
        val saved = fireOperations.persistFireFile(accNo, file, FILE, file.md5(), "$subFolder/$fName")

        return FireFile(fName, fName, saved.fireOid, saved.objectMd5, saved.objectSize.toLong(), FILE, listOf())
    }
}
