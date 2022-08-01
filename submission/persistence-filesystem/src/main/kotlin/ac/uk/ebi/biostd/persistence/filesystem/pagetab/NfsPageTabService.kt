package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import uk.ac.ebi.extended.serialization.service.TrackSection
import java.io.File

private val logger = KotlinLogging.logger {}

class NfsPageTabService(
    private val folderResolver: SubmissionFolderResolver,
    private val pageTabUtil: PageTabUtil,
    private val fileProcessingService: FileProcessingService,
) : PageTabService {
    override fun generatePageTab(sub: ExtSubmission): ExtSubmission {
        logger.info { "${sub.accNo} ${sub.owner} Generating page tab files" }

        val submissionFolder = folderResolver.getSubFolder(sub.relPath).toFile()
        val filesFolder = submissionFolder.resolve(FILES_PATH)

        logger.info { "${sub.accNo} ${sub.owner} Generating submission page tab files" }
        val subFiles = pageTabUtil.generateSubPageTab(sub, submissionFolder)

        logger.info { "${sub.accNo} ${sub.owner} Generating submission file list page tab files" }
        val fileListFiles = pageTabUtil.generateFileListPageTab(sub, filesFolder)
        val section = fileProcessingService.process(sub.section) { updateFileList(it, fileListFiles) }

        return when {
            section.changed -> sub.copy(pageTabFiles = subFiles(subFiles), section = section.section)
            else -> sub.copy(pageTabFiles = subFiles(subFiles))
        }
    }

    private fun updateFileList(sec: ExtSection, tab: Map<String, PageTabFiles>): TrackSection {
        return when (val lst = sec.fileList) {
            null -> TrackSection(false, sec)
            else -> {
                val fileName = lst.filePath
                val tabFiles = tab.getValue(lst.filePath)
                TrackSection(true, sec.copy(fileList = lst.copy(pageTabFiles = fileListFiles(tabFiles, fileName))))
            }
        }
    }

    private fun fileListFiles(files: PageTabFiles, name: String): List<NfsFile> {
        val (json, xml, tsv) = files
        return listOf(
            createNfsFile("$name.json", json),
            createNfsFile("$name.xml", xml),
            createNfsFile("$name.tsv", tsv),
        )
    }

    private fun createNfsFile(name: String, file: File): NfsFile =
        NfsFile(name, "Files/$name", file, file.absolutePath, file.md5(), file.size())

    private fun subFiles(files: PageTabFiles): List<NfsFile> {
        val (json, xml, tsv) = files
        return listOf(
            NfsFile(json.name, json.name, json, json.absolutePath, json.md5(), json.size()),
            NfsFile(xml.name, xml.name, xml, xml.absolutePath, xml.md5(), xml.size()),
            NfsFile(tsv.name, tsv.name, tsv, tsv.absolutePath, tsv.md5(), tsv.size())
        )
    }
}
