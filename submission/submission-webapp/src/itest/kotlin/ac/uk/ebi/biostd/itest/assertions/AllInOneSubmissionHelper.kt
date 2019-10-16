package ac.uk.ebi.biostd.itest.assertions

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.XML
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.factory.allInOneSubmission
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionXml
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionXml
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.test.createFile
import ebi.ac.uk.test.createOrReplaceDirectory
import ebi.ac.uk.test.createOrReplaceFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import org.assertj.core.api.Assertions.assertThat
import java.nio.file.Paths

internal class AllInOneSubmissionHelper(
    private val basePath: String,
    private val submissionRepository: SubmissionRepository
) {
    internal fun submitAllInOneSubmission(
        accNo: String,
        format: SubmissionFormat,
        webClient: BioWebClient,
        tempFolder: TemporaryFolder
    ) {
        val submission = when (format) {
            TSV -> allInOneSubmissionTsv(accNo).toString()
            XML -> allInOneSubmissionXml(accNo).toString()
            JSON -> allInOneSubmissionJson(accNo).toString()
        }

        createSubmissionFiles(webClient, tempFolder)
        assertThat(webClient.submitSingle(submission, format)).isSuccessful()
    }

    internal fun submitAllInOneMultipartSubmission(
        accNo: String,
        format: SubmissionFormat,
        webClient: BioWebClient,
        tempFolder: TemporaryFolder
    ) {
        val submission = when (format) {
            TSV -> tempFolder.createFile("$accNo.tsv", allInOneSubmissionTsv(accNo).toString())
            XML -> tempFolder.createFile("$accNo.xml", allInOneSubmissionXml(accNo).toString())
            JSON -> tempFolder.createFile("$accNo.json", allInOneSubmissionJson(accNo).toString())
        }

        createSubmissionFiles(webClient, tempFolder)
        assertThat(webClient.submitSingle(submission, emptyList())).isSuccessful()
    }

    internal fun assertSavedSubmission(accNo: String) {
        val extendedSubmission = submissionRepository.getExtendedByAccNo(accNo)
        assertThat(extendedSubmission.asSubmission()).isEqualTo(allInOneSubmission(accNo))
        assertSubmissionFiles(extendedSubmission)
    }

    private fun createSubmissionFiles(webClient: BioWebClient, tempFolder: TemporaryFolder) {
        tempFolder.createOrReplaceDirectory("Folder1")
        tempFolder.createOrReplaceDirectory("Folder1/Folder2")

        webClient.uploadFiles(listOf(tempFolder.createOrReplaceFile("Folder1/DataFile3.txt")), "Folder1")
        webClient.uploadFiles(
            listOf(tempFolder.createOrReplaceFile("Folder1/Folder2/DataFile4.txt")), "Folder1/Folder2")
        webClient.uploadFiles(
            listOf(tempFolder.createOrReplaceFile("DataFile1.txt"), tempFolder.createOrReplaceFile("DataFile2.txt")))
    }

    private fun assertSubmissionFiles(submission: ExtendedSubmission) {
        val submissionFolderPath = "$basePath/submission/${submission.relPath}"
        val accNo = submission.accNo

        assertAllInOneSubmissionXml(getSubFileContent("$submissionFolderPath/$accNo.xml"), accNo)
        assertAllInOneSubmissionJson(getSubFileContent("$submissionFolderPath/$accNo.json"), accNo)
        assertAllInOneSubmissionTsv(getSubFileContent("$submissionFolderPath/$accNo.pagetab.tsv"), accNo)
    }

    private fun getSubFileContent(path: String): String {
        val filePath = Paths.get(path)
        assertThat(filePath).exists()

        return filePath.toFile().readText()
    }
}
