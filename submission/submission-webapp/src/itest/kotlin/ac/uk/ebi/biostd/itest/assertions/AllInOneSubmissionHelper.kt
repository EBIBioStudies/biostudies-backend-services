package ac.uk.ebi.biostd.itest.assertions

import ac.uk.ebi.biostd.itest.factory.allInOneSubmission
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionXml
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import org.assertj.core.api.Assertions.assertThat
import java.io.File
import java.nio.file.Paths

internal class AllInOneSubmissionHelper(
    private val submissionPath: String,
    private val submissionRepository: SubmissionQueryService
) {

    internal fun assertSavedSubmission(
        accNo: String,
        method: ExtSubmissionMethod = ExtSubmissionMethod.PAGE_TAB,
    ) {
        val extendedSubmission = submissionRepository.getExtByAccNo(accNo)

        assertThat(extendedSubmission.status).isEqualTo(ExtProcessingStatus.PROCESSED)
        assertThat(extendedSubmission.method).isEqualTo(method)
        assertThat(extendedSubmission.toSimpleSubmission()).isEqualTo(allInOneSubmission(accNo))
        assertSubmissionFiles(extendedSubmission)
    }

    fun assertSubmissionFilesRecords(accNo: String) {
        val submission = submissionRepository.getExtByAccNo(accNo)
        val submissionFolderPath = "$submissionPath/${submission.relPath}"

        assertSubmissionPageTabs(accNo, submission.pageTabFiles, submissionFolderPath)
        assertFileListPageTabs(submission.section.fileList!!.pageTabFiles, submissionFolderPath)
    }

    private fun assertSubmissionFiles(submission: ExtSubmission) {
        val submissionFolderPath = "$submissionPath/${submission.relPath}"
        val accNo = submission.accNo

        assertAllInOneSubmissionXml(getSubFileContent("$submissionFolderPath/$accNo.xml"), accNo)
        assertAllInOneSubmissionJson(getSubFileContent("$submissionFolderPath/$accNo.json"), accNo)
        assertAllInOneSubmissionTsv(getSubFileContent("$submissionFolderPath/$accNo.pagetab.tsv"), accNo)
    }

    private fun assertSubmissionPageTabs(
        accNo: String,
        pageTabFiles: List<ExtFile>,
        submissionFolderPath: String,
    ) {
        assertThat(pageTabFiles).hasSize(3)

        val jsonFile = File("$submissionFolderPath/$accNo.json")
        assertThat(jsonFile).exists()
        assertThat(pageTabFiles.first()).isEqualTo(
            NfsFile(
                fileName = "$accNo.json",
                "$accNo.json",
                "$accNo.json",
                jsonFile.absolutePath,
                jsonFile
            )
        )

        val xmlFile = File("$submissionFolderPath/$accNo.xml")
        assertThat(xmlFile).exists()
        assertThat(pageTabFiles.second()).isEqualTo(
            NfsFile(
                fileName = "$accNo.xml",
                "$accNo.xml",
                "$accNo.xml",
                xmlFile.absolutePath,
                xmlFile
            )
        )

        val tsvFile = File("$submissionFolderPath/$accNo.pagetab.tsv")
        assertThat(tsvFile).exists()
        assertThat(pageTabFiles.third()).isEqualTo(
            NfsFile(
                fileName = "$accNo.pagetab.tsv",
                "$accNo.pagetab.tsv",
                "$accNo.pagetab.tsv",
                tsvFile.absolutePath,
                tsvFile
            )
        )
    }

    private fun assertFileListPageTabs(
        pageTabFiles: List<ExtFile>,
        submissionFolderPath: String,
    ) {
        assertThat(pageTabFiles).hasSize(3)

        val jsonFile = File("$submissionFolderPath/Files/file-list.json")
        assertThat(jsonFile).exists()
        assertThat(pageTabFiles.first()).isEqualTo(
            NfsFile(
                fileName = "file-list.json",
                "file-list.json",
                "Files/file-list.json",
                jsonFile.absolutePath,
                jsonFile
            )
        )

        val xmlFile = File("$submissionFolderPath/Files/file-list.xml")
        assertThat(xmlFile).exists()
        assertThat(pageTabFiles.second()).isEqualTo(
            NfsFile(
                fileName = "file-list.xml",
                "file-list.xml",
                "Files/file-list.xml",
                xmlFile.absolutePath,
                xmlFile
            )
        )

        val tsvFile = File("$submissionFolderPath/Files/file-list.pagetab.tsv")
        assertThat(tsvFile).exists()
        assertThat(pageTabFiles.third()).isEqualTo(
            NfsFile(
                fileName = "file-list.pagetab.tsv",
                "file-list.pagetab.tsv",
                "Files/file-list.pagetab.tsv",
                tsvFile.absolutePath,
                tsvFile
            )
        )
    }

    private fun getSubFileContent(path: String): String {
        val filePath = Paths.get(path)
        assertThat(filePath).exists()

        return filePath.toFile().readText()
    }
}
