package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.persistence.filesystem.api.PageTabService
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import uk.ac.ebi.extended.serialization.service.TrackSection
import java.io.File

class PageTabService(
    private val fireTempFolder: File,
    private val pageTabUtil: PageTabUtil,
    private val processingService: FileProcessingService,
) : PageTabService {
    override fun generatePageTab(sub: ExtSubmission): ExtSubmission {
        val subFiles = pageTabUtil.generateSubPageTab(sub, fireTempFolder)
        val fileListFiles = pageTabUtil.generateFileListPageTab(sub, fireTempFolder)
        val section = processingService.process(sub.section) { withTabFiles(it, fileListFiles) }
        return when {
            section.changed -> sub.copy(pageTabFiles = subExtFiles(sub.accNo, subFiles), section = section.section)
            else -> sub.copy(pageTabFiles = subExtFiles(sub.accNo, subFiles))
        }
    }

    private fun withTabFiles(
        sec: ExtSection,
        pageTabFiles: Map<String, PageTabFiles>,
    ): TrackSection {
        return when (val fileList = sec.fileList) {
            null -> TrackSection(changed = false, section = sec)
            else -> {
                val name = fileList.filePath
                val files = pageTabFiles.getValue(name)
                TrackSection(
                    changed = true,
                    section = sec.copy(fileList = fileList.copy(pageTabFiles = fileListFiles(files, name)))
                )
            }
        }
    }

    private fun fileListFiles(pageTab: PageTabFiles, fileListName: String): List<NfsFile> = listOf(
        createNfsFile("$fileListName.json", "Files/$fileListName.json", pageTab.json),
        createNfsFile("$fileListName.xml", "Files/$fileListName.xml", pageTab.xml),
        createNfsFile("$fileListName.tsv", "Files/$fileListName.tsv", pageTab.tsv)
    )

    private fun subExtFiles(accNo: String, pageTab: PageTabFiles): List<NfsFile> = listOf(
        createNfsFile("$accNo.json", "$accNo.json", pageTab.json),
        createNfsFile("$accNo.xml", "$accNo.xml", pageTab.xml),
        createNfsFile("$accNo.tsv", "$accNo.tsv", pageTab.tsv)
    )

    fun createNfsFile(path: String, relPath: String, file: File): NfsFile =
        NfsFile(
            filePath = path,
            relPath = relPath,
            file = file,
            fullPath = file.absolutePath,
            md5 = file.md5(),
            size = file.size(),
            attributes = emptyList()
        )
}
