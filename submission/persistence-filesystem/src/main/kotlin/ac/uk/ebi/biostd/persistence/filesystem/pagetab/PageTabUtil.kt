package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.permissions
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.ext.newFile
import java.io.File

class PageTabUtil(
    private val serializationService: SerializationService,
    private val toSubmissionMapper: ToSubmissionMapper,
    private val fileListMapper: ToFileListMapper,
) {
    suspend fun generateSubPageTab(sub: ExtSubmission, target: File): PageTabFiles {
        val element = toSubmissionMapper.toSimpleSubmission(sub)
        val permissions = sub.permissions()

        return PageTabFiles(
            json = saveTabFile(
                target.resolve("${sub.accNo}.json"),
                serializationService.serializeSubmission(element, JSON_PRETTY),
                permissions
            ),
            xml = saveTabFile(
                target.resolve("${sub.accNo}.xml"),
                serializationService.serializeSubmission(element, XML),
                permissions
            ),
            tsv = saveTabFile(
                target.resolve("${sub.accNo}.tsv"),
                serializationService.serializeSubmission(element, TSV),
                permissions
            ),
        )
    }

    suspend fun generateFileListPageTab(submission: ExtSubmission, filesFolder: File): Map<String, PageTabFiles> =
        submission.allFileList.associate { it.filePath to saveTabFiles(filesFolder, it) }

    private suspend fun saveTabFiles(filesDir: File, fileList: ExtFileList): PageTabFiles {
        createFolderStructure(filesDir, fileList.filePath)
        val path = fileList.filePath
        return PageTabFiles(
            json = fileListMapper.serialize(fileList, JSON_PRETTY, filesDir.newFile("$path.json")),
            xml = fileListMapper.serialize(fileList, XML, filesDir.newFile("$path.xml")),
            tsv = fileListMapper.serialize(fileList, TSV, filesDir.newFile("$path.tsv"))
        )
    }

    private fun createFolderStructure(folder: File, fileListPath: String) {
        val file = folder.resolve(fileListPath).parentFile
        file.mkdirs()
    }

    private fun saveTabFile(file: File, content: String, permissions: Permissions): File {
        FileUtils.writeContent(file, content, permissions)
        return file
    }
}

data class PageTabFiles(val json: File, val xml: File, val tsv: File)
