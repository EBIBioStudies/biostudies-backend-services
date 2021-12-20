package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.permissions
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.Permissions
import java.io.File

fun SerializationService.generateSubPageTab(sub: ExtSubmission, target: File): PageTabFiles {
    val element = sub.toSimpleSubmission()
    val permissions = sub.permissions()
    return PageTabFiles(
        json = saveTabFile(target.resolve("${sub.accNo}.json"), serializeSubmission(element, JSON_PRETTY), permissions),
        xml = saveTabFile(target.resolve("${sub.accNo}.xml"), serializeSubmission(element, XML), permissions),
        tsv = saveTabFile(target.resolve("${sub.accNo}.pagetab.tsv"), serializeSubmission(element, TSV), permissions),
    )
}

fun SerializationService.generateFileListPageTab(
    sub: ExtSubmission,
    target: File
): Map<String, PageTabFiles> = sub
    .allFileList
    .associate { it.filePath to saveTabFiles(target, it, sub.permissions()) }

// TODO: create file with permission first
private fun SerializationService.saveTabFiles(
    folder: File,
    fileList: ExtFileList,
    permissions: Permissions
): PageTabFiles {
    val filename = fileList.filePath
    val files = fileList.toFilesTable()
    return PageTabFiles(
        json = serializeFileList(files, JSON_PRETTY, folder.resolve("$filename.json")),
        xml = serializeFileList(files, XML, folder.resolve("$filename.xml")),
        tsv = serializeFileList(files, TSV, folder.resolve("$filename.pagetab.tsv"))
    )
}

private fun saveTabFile(file: File, content: String, permissions: Permissions): File {
    FileUtils.writeContent(file, content, permissions)
    return file
}

data class PageTabFiles(val json: File, val xml: File, val tsv: File)
