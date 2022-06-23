package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.common.TsvPagetabExtension
import ac.uk.ebi.biostd.persistence.filesystem.extensions.persistFireFile
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.io.ext.md5
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import uk.ac.ebi.extended.serialization.service.TrackSection
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.FileType
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
        val section = processingService.process(sub.section) { withTabFiles(sub.accNo, it, sub.relPath, fileListFiles) }
        return when {
            section.changed -> sub.copy(
                pageTabFiles = subExtFiles(sub.accNo, subFiles, sub.relPath),
                section = section.section
            )
            else -> sub.copy(pageTabFiles = subExtFiles(sub.accNo, subFiles, sub.relPath))
        }
    }

    private fun withTabFiles(
        accNo: String,
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
                    section = sec.copy(fileList = lst.copy(pageTabFiles = fileListFiles(accNo, files, path, name)))
                )
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
        val fireFile = fireClient.persistFireFile(accNo, file, FileType.FILE, file.md5(), "$subFolder/$relPath")

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
        val saved = fireClient.persistFireFile(accNo, file, FileType.FILE, file.md5(), "$subFolder/$fName")

        return FireFile(fName, fName, saved.fireOid, saved.objectMd5, saved.objectSize.toLong(), FILE, listOf())
    }
}
