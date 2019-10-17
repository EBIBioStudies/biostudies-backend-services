package ac.uk.ebi.biostd.itest.assertions

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionXml
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.test.createFile
import ebi.ac.uk.test.createOrReplaceDirectory
import ebi.ac.uk.test.createOrReplaceFile
import io.github.glytching.junit.extension.folder.TemporaryFolder

internal fun BioWebClient.submitAllInOneSubmission(
    accNo: String,
    format: SubmissionFormat,
    tempFolder: TemporaryFolder
) {
    val submission = when (format) {
        SubmissionFormat.TSV -> allInOneSubmissionTsv(accNo).toString()
        SubmissionFormat.XML -> allInOneSubmissionXml(accNo).toString()
        SubmissionFormat.JSON -> allInOneSubmissionJson(accNo).toString()
    }

    createSubmissionFiles(tempFolder)
    assertThat(submitSingle(submission, format)).isSuccessful()
}

internal fun BioWebClient.submitAllInOneMultipartSubmission(
    accNo: String,
    format: SubmissionFormat,
    tempFolder: TemporaryFolder
) {
    val submission = when (format) {
        SubmissionFormat.TSV -> tempFolder.createFile("$accNo.tsv", allInOneSubmissionTsv(accNo).toString())
        SubmissionFormat.XML -> tempFolder.createFile("$accNo.xml", allInOneSubmissionXml(accNo).toString())
        SubmissionFormat.JSON -> tempFolder.createFile("$accNo.json", allInOneSubmissionJson(accNo).toString())
    }

    createSubmissionFiles(tempFolder)
    assertThat(submitSingle(submission, emptyList())).isSuccessful()
}

private fun BioWebClient.createSubmissionFiles(tempFolder: TemporaryFolder) {
    tempFolder.createOrReplaceDirectory("Folder1")
    tempFolder.createOrReplaceDirectory("Folder1/Folder2")

    uploadFiles(listOf(tempFolder.createOrReplaceFile("Folder1/DataFile3.txt")), "Folder1")
    uploadFiles(listOf(tempFolder.createOrReplaceFile("Folder1/Folder2/DataFile4.txt")), "Folder1/Folder2")
    uploadFiles(listOf(tempFolder.createOrReplaceFile("DataFile1.txt"), tempFolder.createOrReplaceFile("DataFile2.txt")))
}
