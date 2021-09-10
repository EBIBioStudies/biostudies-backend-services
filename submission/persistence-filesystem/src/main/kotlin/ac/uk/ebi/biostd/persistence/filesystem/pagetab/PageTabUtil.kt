package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.permissions
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.Permissions
import java.io.File

fun SerializationService.generateSubPageTab(
    sub: ExtSubmission,
    target: File
): TabFiles = saveTabFiles(target, sub.accNo, sub.toSimpleSubmission(), sub.permissions())

fun SerializationService.generateFileListPageTab(
    sub: ExtSubmission,
    target: File
): Map<String, TabFiles> = sub
    .allFileList
    .associate { it.fileName to saveTabFiles(target, it.fileName, it.toFilesTable(), sub.permissions()) }

private fun <T> SerializationService.saveTabFiles(
    folder: File,
    filename: String,
    element: T,
    permissions: Permissions
): TabFiles {
    val json = serializeElement(element, SubFormat.JSON_PRETTY)
    val xml = serializeElement(element, SubFormat.XML)
    val tsv = serializeElement(element, SubFormat.TSV)

    val jsonFile = folder.resolve("$filename.json")
    val xmlFile = folder.resolve("$filename.xml")
    val tsvFile = folder.resolve("$filename.pagetab.tsv")

    FileUtils.writeContent(jsonFile, json, permissions)
    FileUtils.writeContent(xmlFile, xml, permissions)
    FileUtils.writeContent(tsvFile, tsv, permissions)

    return TabFiles(jsonFile, xmlFile, tsvFile)
}

data class TabFiles(val json: File, val xml: File, val tsv: File)
