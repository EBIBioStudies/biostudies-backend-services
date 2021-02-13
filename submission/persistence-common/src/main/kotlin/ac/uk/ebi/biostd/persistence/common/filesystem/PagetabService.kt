package ac.uk.ebi.biostd.persistence.common.filesystem

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.io.FileUtils.writeContent
import ebi.ac.uk.model.Submission
import mu.KotlinLogging
import java.io.File
import java.nio.file.attribute.PosixFilePermission

private val logger = KotlinLogging.logger {}

data class PagetabRequest(
    val submission: ExtSubmission,
    val submissionFolder: File,
    val filePermissions: Set<PosixFilePermission>,
    val folderPermissions: Set<PosixFilePermission>
)

class PagetabService(
    private val serializationService: SerializationService
) {
    fun pageTab(request: PagetabRequest) {
        val (submission, submissionFolder, filePermissions, folderPermissions) = request
        pageTab(
            submission = submission.toSimpleSubmission(),
            accNo = submission.accNo,
            fileList = submission.allFileList,
            subFolder = submissionFolder,
            filePermissions = filePermissions,
            folderPermissions = folderPermissions
        )
    }

    fun pageTab(
        submission: Submission,
        accNo: String,
        fileList: List<ExtFileList>,
        subFolder: File,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) {
        logger.info { "generating submission $accNo pagetab files" }

        pageTab(submission, subFolder, accNo, filePermissions, folderPermissions)
        fileList.forEach { pageTab(it.toFilesTable(), subFolder, it.fileName, filePermissions, folderPermissions) }

        logger.info { "pagetab generated successfully for submission $accNo" }
    }

    private fun <T> pageTab(
        element: T,
        submissionFolder: File,
        fileName: String,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) {
        val json = serializationService.serializeElement(element, SubFormat.JSON_PRETTY)
        val xml = serializationService.serializeElement(element, SubFormat.XML)
        val tsv = serializationService.serializeElement(element, SubFormat.TSV)

        writeContent(submissionFolder.resolve("$fileName.json"), json, filePermissions, folderPermissions)
        writeContent(submissionFolder.resolve("$fileName.xml"), xml, filePermissions, folderPermissions)
        writeContent(submissionFolder.resolve("$fileName.pagetab.tsv"), tsv, filePermissions, folderPermissions)
    }
}
