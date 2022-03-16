package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.permissions
import ebi.ac.uk.extended.mapping.to.ToFilesTable
import ebi.ac.uk.extended.mapping.to.ToSubmission
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.Permissions
import java.io.File

class PageTabUtil(private val toSubmission: ToSubmission, private val toFilesTable: ToFilesTable) {
    fun generateSubPageTab(serializationService: SerializationService, sub: ExtSubmission, target: File): PageTabFiles {
        val element = toSubmission.toSimpleSubmission(sub)
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
        serializationService: SerializationService,
        sub: ExtSubmission,
        target: File,
    ): Map<String, PageTabFiles> = sub
        .allFileList
        .associate { it.filePath to serializationService.saveTabFiles(target, it) }

    // TODO: create file with permission first
    private fun SerializationService.saveTabFiles(
        folder: File,
        fileList: ExtFileList,
    ): PageTabFiles {
        val filename = fileList.filePath
        val files = toFilesTable.convert(fileList)

        val json = folder.resolve("$filename.json")
        val xml = folder.resolve("$filename.xml")
        val tsv = folder.resolve("$filename.pagetab.tsv")

        folder.resolve(filename).parentFile.mkdirs()
        json.createNewFile()
        xml.createNewFile()
        tsv.createNewFile()

        return PageTabFiles(
            json = serializeFileList(files, JSON_PRETTY, json),
            xml = serializeFileList(files, XML, xml),
            tsv = serializeFileList(files, TSV, tsv)
        )
    }

    private fun saveTabFile(file: File, content: String, permissions: Permissions): File {
        FileUtils.writeContent(file, content, permissions)
        return file
    }
}

data class PageTabFiles(val json: File, val xml: File, val tsv: File)
