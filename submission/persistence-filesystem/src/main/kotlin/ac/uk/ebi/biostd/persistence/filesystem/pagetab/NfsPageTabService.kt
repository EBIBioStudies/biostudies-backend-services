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
            else -> Section(true, sec.copy(fileList = lst.copy(pageTabFiles = extFiles(tab.getValue(lst.fileName), "Files"))))
        }
    }

    private fun extFiles(files: TabFiles, relpath: String? = null): List<NfsFile> {
        val getRelPath: (String) -> String = { if (relpath == null) it else "$relpath/$it" }
        return listOf(
            NfsFile(files.json.name, files.json.name, getRelPath(files.json.name), files.json.absolutePath, files.json),
            NfsFile(files.xml.name, files.xml.name, getRelPath(files.xml.name), files.xml.absolutePath, files.xml),
            NfsFile(files.tsv.name, files.tsv.name, getRelPath(files.tsv.name), files.tsv.absolutePath, files.tsv)
        )
    }
}
