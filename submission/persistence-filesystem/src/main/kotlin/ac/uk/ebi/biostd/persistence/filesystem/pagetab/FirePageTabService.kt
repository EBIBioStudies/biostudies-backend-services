package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.common.TsvPagetabExtension
import ac.uk.ebi.biostd.persistence.filesystem.extensions.persistFireFile
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import uk.ac.ebi.extended.serialization.service.TrackSection
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.io.File

class FirePageTabService(
    private val fireTempFolder: File,
    private val fireClient: FireClient,
    private val pageTabUtil: PageTabUtil,
    private val processingService: FileProcessingService,
    private val tsvPagetabExtension: TsvPagetabExtension,
) : PageTabService {
    override fun generatePageTab(sub: ExtSubmission): ExtSubmission {
        val subFiles = pageTabUtil.generateSubPageTab(sub, fireTempFolder)
        val fileListFiles = pageTabUtil.generateFileListPageTab(sub, fireTempFolder)
        val section = processingService.process(sub.section) { withTabFiles(it, sub.relPath, fileListFiles) }
        return when {
            section.changed -> sub.copy(
                pageTabFiles = subExtFiles(subFiles, sub.relPath),
                section = section.section
            )
            else -> sub.copy(pageTabFiles = subExtFiles(subFiles, sub.relPath))
        }
    }

    private fun withTabFiles(
        sec: ExtSection,
        path: String,
        pageTabFiles: Map<String, PageTabFiles>,
    ): TrackSection {
        return when (val lst = sec.fileList) {
            null -> TrackSection(changed = false, section = sec)
            else -> {
                val name = lst.filePath
                val files = pageTabFiles.getValue(name)
                TrackSection(
                    changed = true,
                    section = sec.copy(fileList = lst.copy(pageTabFiles = fileListFiles(files, path, name)))
                )
            }
        }
    }

    private fun fileListFiles(pageTab: PageTabFiles, subFolder: String, fileListName: String) = listOf(
        saveFileListFile(pageTab.json, subFolder, "$fileListName.json"),
        saveFileListFile(pageTab.xml, subFolder, "$fileListName.xml"),
        saveFileListFile(pageTab.tsv, subFolder, "$fileListName.${tsvPagetabExtension.tsvExtension()}")
    )

    private fun saveFileListFile(file: File, subFolder: String, filePath: String): FireFile {
        val relPath = "Files/$filePath"
        val save = fireClient.persistFireFile(file, file.md5(), file.size(), "$subFolder/$relPath")
        return FireFile(filePath, relPath, save.fireOid, save.objectMd5, save.objectSize.toLong(), FILE, listOf())
    }

    private fun subExtFiles(pageTab: PageTabFiles, subFolder: String): List<ExtFile> = listOf(
        saveSubFile(pageTab.json, subFolder),
        saveSubFile(pageTab.xml, subFolder),
        saveSubFile(pageTab.tsv, subFolder)
    )

    private fun saveSubFile(file: File, subFolder: String): FireFile {
        val fName = file.name
        val saved = fireClient.persistFireFile(file, file.md5(), file.size(), "$subFolder/$fName")
        return FireFile(fName, fName, saved.fireOid, saved.objectMd5, saved.objectSize.toLong(), FILE, listOf())
    }
}
