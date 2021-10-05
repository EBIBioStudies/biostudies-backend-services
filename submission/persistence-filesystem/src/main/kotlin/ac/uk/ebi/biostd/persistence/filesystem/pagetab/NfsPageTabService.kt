package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.filesystem.service.Section
import ac.uk.ebi.biostd.persistence.filesystem.service.process
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.paths.SubmissionFolderResolver

class NfsPageTabService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) : PageTabService {
    override fun generatePageTab(sub: ExtSubmission): ExtSubmission {
        val submissionFolder = folderResolver.getSubFolder(sub.relPath).toFile()
        val subFiles = serializationService.generateSubPageTab(sub, submissionFolder)
        val fileListFiles = serializationService.generateFileListPageTab(sub, submissionFolder.resolve("Files"))
        val section = process(sub.section) { updateFileList(it, fileListFiles) }

        return when {
            section.changed -> sub.copy(pageTabFiles = extFiles(subFiles), section = section.section)
            else -> sub.copy(pageTabFiles = extFiles(subFiles))
        }
    }

    private fun updateFileList(sec: ExtSection, tab: Map<String, TabFiles>): Section {
        return when (val lst = sec.fileList) {
            null -> Section(false, sec)
            else -> Section(true, sec.copy(fileList = lst.copy(pageTabFiles = extFiles(tab.getValue(lst.fileName)))))
        }
    }

    private fun extFiles(tabsFiles: TabFiles): List<NfsFile> {
        return listOf(
            NfsFile(tabsFiles.json.name, tabsFiles.json),
            NfsFile(tabsFiles.xml.name, tabsFiles.xml),
            NfsFile(tabsFiles.tsv.name, tabsFiles.tsv)
        )
    }
}
