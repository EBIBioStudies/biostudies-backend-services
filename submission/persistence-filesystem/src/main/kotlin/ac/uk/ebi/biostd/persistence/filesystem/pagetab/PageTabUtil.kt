package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.filePermissions
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.folderPermissions
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.io.FileUtils
import java.io.File
import java.nio.file.attribute.PosixFilePermission

fun SerializationService.generatePageTab(
    submission: ExtSubmission,
    subFolder: File,
    filesFolder: File
): List<TabFiles> {
    val accNo = submission.accNo
    val permissions = Permissions(submission.filePermissions(), submission.folderPermissions())

    return listOf(
        saveTabFiles(subFolder, accNo, submission.toSimpleSubmission(), permissions)
    ) + submission.allFileList.map { saveTabFiles(filesFolder, it.fileName, it.toFilesTable(), permissions) }
}

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

    FileUtils.writeContent(jsonFile, json, permissions.file, permissions.folder)
    FileUtils.writeContent(xmlFile, xml, permissions.file, permissions.folder)
    FileUtils.writeContent(tsvFile, tsv, permissions.file, permissions.folder)

    return TabFiles(jsonFile, xmlFile, tsvFile)
}

data class Permissions(val file: Set<PosixFilePermission>, val folder: Set<PosixFilePermission>)
data class TabFiles(val json: File, val xml: File, val tsv: File)
