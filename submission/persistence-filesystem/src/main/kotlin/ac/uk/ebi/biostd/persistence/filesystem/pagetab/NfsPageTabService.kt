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
            NfsFile("$name.json", "Files/$name.json", tab.json.absolutePath, tab.json),
            NfsFile("$name.xml", "Files/$name.xml", tab.xml.absolutePath, tab.xml),
            NfsFile("$name.pagetab.tsv", "Files/$name.pagetab.tsv", tab.tsv.absolutePath, tab.tsv)
        )
    }

    private fun subFiles(files: PageTabFiles): List<NfsFile> {
        return listOf(
            NfsFile(files.json.name, files.json.name, files.json.absolutePath, files.json),
            NfsFile(files.xml.name, files.xml.name, files.xml.absolutePath, files.xml),
            NfsFile(files.tsv.name, files.tsv.name, files.tsv.absolutePath, files.tsv)
        )
    }
}
