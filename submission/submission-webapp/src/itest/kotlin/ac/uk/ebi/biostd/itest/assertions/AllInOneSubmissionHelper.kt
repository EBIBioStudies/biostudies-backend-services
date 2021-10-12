package ac.uk.ebi.biostd.itest.assertions

import ac.uk.ebi.biostd.itest.factory.allInOneSubmission
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionXml
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
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

    private fun assertSubmissionFiles(submission: ExtSubmission) {
        val submissionFolderPath = "$submissionPath/${submission.relPath}"
        val accNo = submission.accNo

        assertAllInOneSubmissionXml(getSubFileContent("$submissionFolderPath/$accNo.xml"), accNo)
        assertAllInOneSubmissionJson(getSubFileContent("$submissionFolderPath/$accNo.json"), accNo)
        assertAllInOneSubmissionTsv(getSubFileContent("$submissionFolderPath/$accNo.pagetab.tsv"), accNo)
    }

    fun assertSubmissionFilesRecordsNfs(accNo: String) {
        val submission = submissionRepository.getExtByAccNo(accNo)
        val submissionFolderPath = "$submissionPath/${submission.relPath}"

        val submissionTabFiles = submission.pageTabFiles as List<NfsFile>
        assertThat(submissionTabFiles).hasSize(3)
        assertThat(submissionTabFiles).isEqualTo(submissionNfsTabFiles(accNo, submissionFolderPath))

        val fileListTabFiles = submission.section.fileList!!.pageTabFiles as List<NfsFile>
        assertThat(fileListTabFiles).hasSize(3)
        assertThat(fileListTabFiles).isEqualTo(fileListNfsTabFiles(submissionFolderPath))
    }

    fun assertSubmissionFilesRecordsFire(accNo: String) {
        val submission = submissionRepository.getExtByAccNo(accNo)
        val subFolder = "$submissionPath/${submission.relPath}"

        val submissionTabFiles = submission.pageTabFiles as List<FireFile>
        assertThat(submissionTabFiles).hasSize(3)
        assertThat(submissionTabFiles).isEqualTo(submissionFireTabFiles(accNo, subFolder))

        val fileListTabFiles = submission.section.fileList!!.pageTabFiles as List<FireFile>
        assertThat(fileListTabFiles).hasSize(3)
        assertThat(fileListTabFiles).isEqualTo(fileListFireTabFiles(subFolder))
    }

    private fun submissionFireTabFiles(accNo: String, subFolder: String): List<FireFile> {
        val json = File("$subFolder/$accNo.json")
        val xmlFile = File("$subFolder/$accNo.xml")
        val tsvFile = File("$subFolder/$accNo.pagetab.tsv")
        return listOf(
            FireFile("$accNo.json", "$accNo.json", "$accNo.json", "$accNo.json", json.md5(), json.size(), listOf()),
            FireFile(
                fileName = "$accNo.xml",
                filePath = "$accNo.xml",
                relPath = "$accNo.xml",
                fireId = "$accNo.xml",
                md5 = xmlFile.md5(),
                size = xmlFile.size(),
                attributes = listOf()
            ),
            FireFile(
                fileName = "$accNo.pagetab.tsv",
                filePath = "$accNo.pagetab.tsv",
                relPath = "$accNo.pagetab.tsv",
                fireId = "$accNo.pagetab.tsv",
                md5 = tsvFile.md5(),
                size = tsvFile.size(),
                attributes = listOf()
            )
        )
    }

    private fun fileListFireTabFiles(subFolder: String): List<FireFile> {
        val fireTempFolder = subFolder.substringBeforeLast("tmp").plus("tmp/tmp/fire-temp")
        val jsonFile = File("$fireTempFolder/file-list.json")
        val xmlFile = File("$fireTempFolder/file-list.xml")
        val tsvFile = File("$fireTempFolder/file-list.pagetab.tsv")
        return listOf(
            FireFile(
                fileName = "file-list.json",
                filePath = "file-list.json",
                relPath = "Files/file-list.json",
                fireId = "file-list.json",
                md5 = jsonFile.md5(),
                size = jsonFile.size(),
                attributes = listOf()
            ),
            FireFile(
                fileName = "file-list.xml",
                filePath = "file-list.xml",
                relPath = "Files/file-list.xml",
                fireId = "file-list.xml",
                md5 = xmlFile.md5(),
                size = xmlFile.size(),
                attributes = listOf()
            ),
            FireFile(
                fileName = "file-list.pagetab.tsv",
                filePath = "file-list.pagetab.tsv",
                relPath = "Files/file-list.pagetab.tsv",
                fireId = "file-list.pagetab.tsv",
                md5 = tsvFile.md5(),
                size = tsvFile.size(),
                attributes = listOf()
            )
        )
    }

    private fun submissionNfsTabFiles(accNo: String, submissionFolderPath: String): List<NfsFile> {
        val jsonFile = File("$submissionFolderPath/$accNo.json")
        val xmlFile = File("$submissionFolderPath/$accNo.xml")
        val tsvFile = File("$submissionFolderPath/$accNo.pagetab.tsv")

        return listOf(
            NfsFile(
                fileName = "$accNo.json",
                "$accNo.json",
                "$accNo.json",
                jsonFile.absolutePath,
                jsonFile
            ),
            NfsFile(
                fileName = "$accNo.xml",
                "$accNo.xml",
                "$accNo.xml",
                xmlFile.absolutePath,
                xmlFile
            ),
            NfsFile(
                fileName = "$accNo.pagetab.tsv",
                "$accNo.pagetab.tsv",
                "$accNo.pagetab.tsv",
                tsvFile.absolutePath,
                tsvFile
            )
        )
    }

    private fun fileListNfsTabFiles(submissionFolderPath: String): List<NfsFile> {
        val jsonFile = File("$submissionFolderPath/Files/file-list.json")
        val xmlFile = File("$submissionFolderPath/Files/file-list.xml")
        val tsvFile = File("$submissionFolderPath/Files/file-list.pagetab.tsv")

        return listOf(
            NfsFile(
                fileName = "file-list.json",
                "file-list.json",
                "Files/file-list.json",
                jsonFile.absolutePath,
                jsonFile
            ),
            NfsFile(
                fileName = "file-list.xml",
                "file-list.xml",
                "Files/file-list.xml",
                xmlFile.absolutePath,
                xmlFile
            ),
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
