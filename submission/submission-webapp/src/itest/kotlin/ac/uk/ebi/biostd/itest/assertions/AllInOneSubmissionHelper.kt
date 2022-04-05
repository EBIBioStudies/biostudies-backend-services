package ac.uk.ebi.biostd.itest.assertions

import ac.uk.ebi.biostd.itest.factory.allInOneSubmission
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionXml
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import arrow.core.Either
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import java.io.File
import java.nio.file.Paths
import org.assertj.core.api.Assertions.assertThat

internal class AllInOneSubmissionHelper(
    private val submissionPath: String,
    private val submissionRepository: SubmissionQueryService,
    private val toSubmissionMapper: ToSubmissionMapper
) {

    internal fun assertSavedSubmission(
        accNo: String,
        method: ExtSubmissionMethod = ExtSubmissionMethod.PAGE_TAB,
    ) {
        val extendedSubmission = submissionRepository.getExtByAccNo(accNo)

        assertThat(extendedSubmission.status).isEqualTo(ExtProcessingStatus.PROCESSED)
        assertThat(extendedSubmission.method).isEqualTo(method)
        assertThat(toSubmissionMapper.toSimpleSubmission(extendedSubmission)).isEqualTo(allInOneSubmission(accNo))
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
        val subFolder = "$submissionPath/${submission.relPath}"

        val submissionTabFiles = submission.pageTabFiles as List<NfsFile>
        assertThat(submissionTabFiles).hasSize(3)
        assertThat(submissionTabFiles).isEqualTo(submissionNfsTabFiles(accNo, subFolder))

        val fileListTabFiles = submission.section.fileList!!.pageTabFiles as List<NfsFile>
        assertThat(fileListTabFiles).hasSize(3)
        assertThat(fileListTabFiles).isEqualTo(nfsTabFiles(subFolder, "file-list"))

        val subFileListTabFiles =
            (submission.section.sections.first() as Either.Left).a.fileList!!.pageTabFiles as List<NfsFile>
        assertThat(subFileListTabFiles).hasSize(3)
        assertThat(subFileListTabFiles).isEqualTo(nfsTabFiles(subFolder, "sub-folder/file-list2"))
    }

    fun assertSubmissionFilesRecordsFire(accNo: String) {
        val submission = submissionRepository.getExtByAccNo(accNo)
        val subFolder = "$submissionPath/${submission.relPath}"

        assertFireTabFiles(submission, accNo, subFolder)
        assertFireFileListTabFiles(submission, subFolder)
        assertFireSubFileListTabFiles(submission, subFolder)
    }

    private fun `assertFireTabFiles`(submission: ExtSubmission, accNo: String, subFolder: String) {
        val submissionTabFiles = submission.pageTabFiles as List<FireFile>
        assertThat(submissionTabFiles).hasSize(3)

        val jsonTabFile = submissionTabFiles.first()
        val jsonFile = File("$subFolder/$accNo.json")
        assertThat(jsonTabFile.filePath).isEqualTo("$accNo.json")
        assertThat(jsonTabFile.relPath).isEqualTo("$accNo.json")
        assertThat(jsonTabFile.fireId).endsWith("_$accNo.json")
        assertThat(jsonTabFile.md5).isEqualTo(jsonFile.md5())
        assertThat(jsonTabFile.size).isEqualTo(jsonFile.size())

        val xmlTabFile = submissionTabFiles.second()
        val xmlFile = File("$subFolder/$accNo.xml")
        assertThat(xmlTabFile.filePath).isEqualTo("$accNo.xml")
        assertThat(xmlTabFile.relPath).isEqualTo("$accNo.xml")
        assertThat(xmlTabFile.fireId).endsWith("_$accNo.xml")
        assertThat(xmlTabFile.md5).isEqualTo(xmlFile.md5())
        assertThat(xmlTabFile.size).isEqualTo(xmlFile.size())

        val tsvTabFile = submissionTabFiles.third()
        val tsvFile = File("$subFolder/$accNo.pagetab.tsv")
        assertThat(tsvTabFile.filePath).isEqualTo("$accNo.pagetab.tsv")
        assertThat(tsvTabFile.relPath).isEqualTo("$accNo.pagetab.tsv")
        assertThat(tsvTabFile.fireId).endsWith("_$accNo.pagetab.tsv")
        assertThat(tsvTabFile.md5).isEqualTo(tsvFile.md5())
        assertThat(tsvTabFile.size).isEqualTo(tsvFile.size())
    }
    private fun `assertFireFileListTabFiles`(submission: ExtSubmission, subFolder: String) {
        val fileListTabFiles = submission.section.fileList!!.pageTabFiles as List<FireFile>
        assertThat(fileListTabFiles).hasSize(3)

        val jsonTabFile = fileListTabFiles.first()
        val jsonFile = File("$subFolder/Files/file-list.json")
        assertThat(jsonTabFile.filePath).isEqualTo("file-list.json")
        assertThat(jsonTabFile.relPath).isEqualTo("Files/file-list.json")
        assertThat(jsonTabFile.fireId).endsWith("file-list.json")
        assertThat(jsonTabFile.md5).isEqualTo(jsonFile.md5())
        assertThat(jsonTabFile.size).isEqualTo(jsonFile.size())

        val xmlTabFile = fileListTabFiles.second()
        val xmlFile = File("$subFolder/Files/file-list.xml")
        assertThat(xmlTabFile.filePath).isEqualTo("file-list.xml")
        assertThat(xmlTabFile.relPath).isEqualTo("Files/file-list.xml")
        assertThat(xmlTabFile.fireId).endsWith("file-list.xml")
        assertThat(xmlTabFile.md5).isEqualTo(xmlFile.md5())
        assertThat(xmlTabFile.size).isEqualTo(xmlFile.size())

        val tsvTabFile = fileListTabFiles.third()
        val tsvFile = File("$subFolder/Files/file-list.pagetab.tsv")
        assertThat(tsvTabFile.filePath).isEqualTo("file-list.pagetab.tsv")
        assertThat(tsvTabFile.relPath).isEqualTo("Files/file-list.pagetab.tsv")
        assertThat(tsvTabFile.fireId).endsWith("_file-list.pagetab.tsv")
        assertThat(tsvTabFile.md5).isEqualTo(tsvFile.md5())
        assertThat(tsvTabFile.size).isEqualTo(tsvFile.size())
    }
    private fun `assertFireSubFileListTabFiles`(submission: ExtSubmission, subFolder: String) {
        val subFileListTabFiles =
            (submission.section.sections.first() as Either.Left).a.fileList!!.pageTabFiles as List<FireFile>
        assertThat(subFileListTabFiles).hasSize(3)

        val jsonTabFile = subFileListTabFiles.first()
        val jsonFile = File("$subFolder/Files/sub-folder/file-list2.json")
        assertThat(jsonTabFile.filePath).isEqualTo("sub-folder/file-list2.json")
        assertThat(jsonTabFile.relPath).isEqualTo("Files/sub-folder/file-list2.json")
        assertThat(jsonTabFile.fireId).endsWith("_file-list2.json")
        assertThat(jsonTabFile.md5).isEqualTo(jsonFile.md5())
        assertThat(jsonTabFile.size).isEqualTo(jsonFile.size())

        val xmlTabFile = subFileListTabFiles.second()
        val xmlFile = File("$subFolder/Files/sub-folder/file-list2.xml")
        assertThat(xmlTabFile.filePath).isEqualTo("sub-folder/file-list2.xml")
        assertThat(xmlTabFile.relPath).isEqualTo("Files/sub-folder/file-list2.xml")
        assertThat(xmlTabFile.fireId).endsWith("_file-list2.xml")
        assertThat(xmlTabFile.md5).isEqualTo(xmlFile.md5())
        assertThat(xmlTabFile.size).isEqualTo(xmlFile.size())

        val tsvTabFile = subFileListTabFiles.third()
        val tsvFile = File("$subFolder/Files/sub-folder/file-list2.pagetab.tsv")
        assertThat(tsvTabFile.filePath).isEqualTo("sub-folder/file-list2.pagetab.tsv")
        assertThat(tsvTabFile.relPath).isEqualTo("Files/sub-folder/file-list2.pagetab.tsv")
        assertThat(tsvTabFile.fireId).endsWith("_file-list2.pagetab.tsv")
        assertThat(tsvTabFile.md5).isEqualTo(tsvFile.md5())
        assertThat(tsvTabFile.size).isEqualTo(tsvFile.size())
    }

    private fun submissionNfsTabFiles(accNo: String, submissionFolderPath: String): List<NfsFile> {
        val jsonFile = File("$submissionFolderPath/$accNo.json")
        val xmlFile = File("$submissionFolderPath/$accNo.xml")
        val tsvFile = File("$submissionFolderPath/$accNo.pagetab.tsv")

        return listOf(
            createNfsFile("$accNo.json", "$accNo.json", jsonFile),
            createNfsFile("$accNo.xml", "$accNo.xml", xmlFile),
            createNfsFile("$accNo.pagetab.tsv", "$accNo.pagetab.tsv", tsvFile)
        )
    }

    private fun nfsTabFiles(subFolder: String, list: String): List<NfsFile> {
        val path = "Files/$list"
        val json = File("$subFolder/$path.json")
        val xml = File("$subFolder/$path.xml")
        val tsv = File("$subFolder/$path.pagetab.tsv")
        return listOf(
            createNfsFile("$list.json", "$path.json", json),
            createNfsFile("$list.xml", "$path.xml", xml),
            createNfsFile("$list.pagetab.tsv", "$path.pagetab.tsv", tsv)
        )
    }

    private fun getSubFileContent(path: String): String {
        val filePath = Paths.get(path)
        assertThat(filePath).exists()

        return filePath.toFile().readText()
    }
}
