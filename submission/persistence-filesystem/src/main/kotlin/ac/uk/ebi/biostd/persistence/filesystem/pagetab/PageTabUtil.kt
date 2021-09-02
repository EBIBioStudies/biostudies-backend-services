package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.io.FileUtils
import java.io.File
import java.nio.file.attribute.PosixFilePermission

internal fun <T> pageTab(
    element: T,
    subFolder: File,
    fileName: String,
    filePermissions: Set<PosixFilePermission>,
    folderPermissions: Set<PosixFilePermission>,
    serializationService: SerializationService
): List<File> {
    val json = serializationService.serializeElement(element, SubFormat.JSON_PRETTY)
    val xml = serializationService.serializeElement(element, SubFormat.XML)
    val tsv = serializationService.serializeElement(element, SubFormat.TSV)

    FileUtils.writeContent(subFolder.resolve("$fileName.json"), json, filePermissions, folderPermissions)
    FileUtils.writeContent(subFolder.resolve("$fileName.xml"), xml, filePermissions, folderPermissions)
    FileUtils.writeContent(subFolder.resolve("$fileName.pagetab.tsv"), tsv, filePermissions, folderPermissions)

    return listOf(
        subFolder.resolve("$fileName.json"),
        subFolder.resolve("$fileName.xml"),
        subFolder.resolve("$fileName.pagetab.tsv")
    )
}