package ac.uk.ebi.biostd.itest.assertions

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.factory.submissionSpecTsv
import ac.uk.ebi.biostd.itest.factory.submissionSpecJson
import ac.uk.ebi.biostd.itest.factory.submissionSpecXml
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.test.createFile
import ebi.ac.uk.test.createOrReplaceDirectory
import ebi.ac.uk.test.createOrReplaceFile
import io.github.glytching.junit.extension.folder.TemporaryFolder

data class SubmissionSpec(val submission: String, val fileList: String)

internal fun BioWebClient.submitAllInOneSubmission(
    accNo: String,
    format: SubmissionFormat,
    tempFolder: TemporaryFolder
) {
    val subSpec: SubmissionSpec
    val submission = when (format) {
        SubmissionFormat.TSV -> {
            subSpec = submissionSpecTsv(accNo)
            createSubmissionFiles(tempFolder, "file-list.tsv", subSpec.fileList)
            subSpec.submission
        }
        SubmissionFormat.XML -> {
            subSpec = submissionSpecXml(accNo)
            createSubmissionFiles(tempFolder, "file-list.xml", subSpec.fileList)
            subSpec.submission
        }
        SubmissionFormat.JSON -> {
            subSpec = submissionSpecJson(accNo)
            createSubmissionFiles(tempFolder, "file-list.json", subSpec.fileList)
            subSpec.submission
        }
    }

    assertThat(submitSingle(submission, format)).isSuccessful()
}

internal fun BioWebClient.submitAllInOneMultipartSubmission(
    accNo: String,
    format: SubmissionFormat,
    tempFolder: TemporaryFolder
) {
    val subSpec: SubmissionSpec
    val submission = when (format) {
        SubmissionFormat.TSV -> {
            subSpec = submissionSpecTsv(accNo)
            createSubmissionFiles(tempFolder, "file-list.tsv", subSpec.fileList)
            tempFolder.createFile("$accNo.tsv", subSpec.submission)
        }
        SubmissionFormat.XML -> {
            subSpec = submissionSpecXml(accNo)
            createSubmissionFiles(tempFolder, "file-list.xml", subSpec.fileList)
            tempFolder.createFile("$accNo.xml", subSpec.submission)
        }
        SubmissionFormat.JSON -> {
            subSpec = submissionSpecJson(accNo)
            createSubmissionFiles(tempFolder, "file-list.json", subSpec.fileList)
            tempFolder.createFile("$accNo.json", subSpec.submission)
        }
    }

    assertThat(submitSingle(submission, emptyList())).isSuccessful()
}

private fun BioWebClient.createSubmissionFiles(
    tempFolder: TemporaryFolder,
    fileListName: String,
    fileListContent: String
) {
    tempFolder.createOrReplaceDirectory("Folder1")
    tempFolder.createOrReplaceDirectory("Folder1/Folder2")

    uploadFiles(listOf(tempFolder.createOrReplaceFile("Folder1/DataFile3.txt")), "Folder1")
    uploadFiles(listOf(tempFolder.createOrReplaceFile("Folder1/DataFile6.txt")), "Folder1")
    uploadFiles(listOf(tempFolder.createOrReplaceFile("Folder1/Folder2/DataFile4.txt")), "Folder1/Folder2")
    uploadFiles(
        listOf(
            tempFolder.createOrReplaceFile("DataFile1.txt"),
            tempFolder.createOrReplaceFile("DataFile2.txt"),
            tempFolder.createOrReplaceFile("DataFile5.txt")
        )
    )
    uploadFiles(listOf(tempFolder.createOrReplaceFile(fileListName, fileListContent)))
}
