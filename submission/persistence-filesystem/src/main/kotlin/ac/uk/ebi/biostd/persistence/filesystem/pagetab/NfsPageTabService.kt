package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileProcessingService
import ac.uk.ebi.biostd.persistence.filesystem.service.Section
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.paths.SubmissionFolderResolver

class NfsPageTabService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService,
    private val pageTabUtil: PageTabUtil,
    private val fileProcessingService: FileProcessingService
) : PageTabService {
    override fun generatePageTab(sub: ExtSubmission): ExtSubmission {
        val submissionFolder = folderResolver.getSubFolder(sub.relPath).toFile()
        val subFiles = pageTabUtil.generateSubPageTab(serializationService, sub, submissionFolder)
        val fileListFiles =
            pageTabUtil.generateFileListPageTab(serializationService, sub, submissionFolder.resolve("Files"))
        val section = fileProcessingService.process(sub.section) { updateFileList(it, fileListFiles) }

        return when {
            section.changed -> sub.copy(pageTabFiles = subFiles(subFiles), section = section.section)
            else -> sub.copy(pageTabFiles = subFiles(subFiles))
        }
    }

    private fun updateFileList(sec: ExtSection, tab: Map<String, PageTabFiles>): Section {
        return when (val lst = sec.fileList) {
            null -> Section(false, sec)
            else -> {
                val fileName = lst.filePath
                val tabFiles = tab.getValue(lst.filePath)
                Section(true, sec.copy(fileList = lst.copy(pageTabFiles = fileListFiles(tabFiles, fileName))))
            }
        }
    }

    private fun fileListFiles(tab: PageTabFiles, name: String): List<NfsFile> {
        return listOf(
            createNfsFile("$name.json", "Files/$name.json", tab.json),
            createNfsFile("$name.xml", "Files/$name.xml", tab.xml),
            createNfsFile("$name.pagetab.tsv", "Files/$name.pagetab.tsv", tab.tsv),
        )
    }

    private fun subFiles(files: PageTabFiles): List<NfsFile> {
        return listOf(
            createNfsFile(files.json.name, files.json.name, files.json),
            createNfsFile(files.xml.name, files.xml.name, files.xml),
            createNfsFile(files.tsv.name, files.tsv.name, files.tsv)
        )
    }
}
