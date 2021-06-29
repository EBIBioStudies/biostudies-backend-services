package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.persistence.filesystem.request.PageTabRequest
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.io.FileUtils.writeContent
import ebi.ac.uk.paths.FILES_PATH
import mu.KotlinLogging
import java.io.File
import java.nio.file.attribute.PosixFilePermission

private val logger = KotlinLogging.logger {}

class PageTabService(private val serializationService: SerializationService) {
    fun generatePageTab(request: PageTabRequest) {
        val (submission, submissionFolder, filePermissions, folderPermissions) = request
        val accNo = submission.accNo
        val filesFolder = submissionFolder.resolve(FILES_PATH)
        logger.info { "generating submission $accNo pagetab files" }

        pageTab(submission.toSimpleSubmission(), submissionFolder, accNo, filePermissions, folderPermissions)
        submission.allFileList.forEach {
            pageTab(it.toFilesTable(), filesFolder, it.fileName, filePermissions, folderPermissions)
        }

        logger.info { "page tab successfully generated for submission $accNo" }
    }

    private fun <T> pageTab(
        element: T,
        submissionFolder: File,
        fileName: String,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) {
        val json = serializationService.serializeElement(element, JSON_PRETTY)
        val xml = serializationService.serializeElement(element, XML)
        val tsv = serializationService.serializeElement(element, TSV)

        writeContent(submissionFolder.resolve("$fileName.json"), json, filePermissions, folderPermissions)
        writeContent(submissionFolder.resolve("$fileName.xml"), xml, filePermissions, folderPermissions)
        writeContent(submissionFolder.resolve("$fileName.pagetab.tsv"), tsv, filePermissions, folderPermissions)
    }
}
