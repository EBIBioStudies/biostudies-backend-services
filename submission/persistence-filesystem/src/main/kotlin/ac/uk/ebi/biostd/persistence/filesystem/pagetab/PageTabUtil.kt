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
import java.io.File

class PageTabUtil(
    private val serializationService: SerializationService,
    private val toSubmissionMapper: ToSubmissionMapper,
    private val toFilesTableMapper: ToFileListMapper,
) {
    fun generateSubPageTab(sub: ExtSubmission, target: File): PageTabFiles {
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
                target.resolve("${sub.accNo}.pagetab.tsv"),
                serializationService.serializeSubmission(element, TSV),
                permissions
            ),
        )
    }

    fun generateFileListPageTab(
        sub: ExtSubmission,
        target: File,
    ): Map<String, PageTabFiles> = sub
        .allFileList
        .associate { it.filePath to saveTabFiles(target, it) }

    // TODO: create file with permission first
    private fun saveTabFiles(
        folder: File,
        fileList: ExtFileList,
    ): PageTabFiles {
        val filename = fileList.filePath
        folder.resolve(filename).parentFile.mkdirs()
        val json = folder.resolve("$filename.json").apply { createNewFile() }
        val xml = folder.resolve("$filename.xml").apply { createNewFile() }
        val tsv = folder.resolve("$filename.pagetab.tsv").apply { createNewFile() }
        return PageTabFiles(
            json = toFilesTableMapper.serialize(fileList, JSON_PRETTY, json),
            xml = toFilesTableMapper.serialize(fileList, XML, xml),
            tsv = toFilesTableMapper.serialize(fileList, TSV, tsv)
        )
    }

    private fun saveTabFile(file: File, content: String, permissions: Permissions): File {
        FileUtils.writeContent(file, content, permissions)
        return file
    }
}

data class PageTabFiles(val json: File, val xml: File, val tsv: File)
