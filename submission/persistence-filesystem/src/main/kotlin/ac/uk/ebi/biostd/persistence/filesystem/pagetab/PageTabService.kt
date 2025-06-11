package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.persistence.filesystem.api.PageTabService
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import uk.ac.ebi.extended.serialization.service.TrackSection
import uk.ac.ebi.extended.serialization.service.iterateSections
import java.io.File

class PageTabService(
    private val baseTempFolder: File,
    private val pageTabUtil: PageTabUtil,
) : PageTabService {
    override suspend fun generatePageTab(sub: ExtSubmission): ExtSubmission {
        val tempFolder = createTempFolder(sub.accNo, sub.version)
        val subFiles = pageTabUtil.generateSubPageTab(sub, tempFolder)
        val fileListFiles = pageTabUtil.generateFileListPageTab(sub, tempFolder)
        val linkListFiles = pageTabUtil.generateLinkListPageTab(sub, tempFolder)
        val section = iterateSections(sub.section) { withTabFiles(it, fileListFiles, linkListFiles) }
        return when {
            section.changed -> sub.copy(pageTabFiles = subExtFiles(sub.accNo, subFiles), section = section.section)
            else -> sub.copy(pageTabFiles = subExtFiles(sub.accNo, subFiles))
        }
    }

    private fun withTabFiles(
        sec: ExtSection,
        fileListTabFiles: Map<String, PageTabFiles>,
        linkListTabFiles: Map<String, PageTabFiles>,
    ): TrackSection {
        if (sec.fileList == null && sec.linkList == null) return TrackSection(changed = false, section = sec)

        var section = sec
        val fileList = sec.fileList
        val linkList = sec.linkList

        if (fileList != null) {
            val name = fileList.filePath
            val files = fileListTabFiles.getValue(name)
            section = section.copy(fileList = fileList.copy(pageTabFiles = refListFiles(files, name)))
        }

        if (linkList != null) {
            val name = linkList.filePath
            val files = linkListTabFiles.getValue(name)
            section = section.copy(linkList = linkList.copy(pageTabFiles = refListFiles(files, name)))
        }

        return TrackSection(changed = true, section = section)
    }

    private fun refListFiles(
        pageTab: PageTabFiles,
        fileListName: String,
    ): List<NfsFile> =
        listOf(
            createNfsFile("$fileListName.json", "Files/$fileListName.json", pageTab.json),
            createNfsFile("$fileListName.tsv", "Files/$fileListName.tsv", pageTab.tsv),
        )

    private fun subExtFiles(
        accNo: String,
        pageTab: PageTabFiles,
    ): List<NfsFile> =
        listOf(
            createNfsFile("$accNo.json", "$accNo.json", pageTab.json),
            createNfsFile("$accNo.tsv", "$accNo.tsv", pageTab.tsv),
        )

    private fun createNfsFile(
        path: String,
        relPath: String,
        file: File,
    ): NfsFile =
        NfsFile(
            filePath = path,
            relPath = relPath,
            file = file,
            fullPath = file.absolutePath,
            md5Calculated = true,
            md5 = file.md5(),
            size = file.size(),
            attributes = emptyList(),
        )

    private fun createTempFolder(
        accNo: String,
        version: Int,
    ): File {
        val path = baseTempFolder.resolve("$accNo/$version")
        path.mkdirs()
        return path
    }
}
