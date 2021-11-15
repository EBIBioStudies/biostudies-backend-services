package ac.uk.ebi.biostd.itest.assertions

import ac.uk.ebi.biostd.itest.factory.allInOneSubmission
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionXml
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import arrow.core.Either
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

        val submissionTabFiles = submission.pageTabFiles as List<FireFile>
        assertThat(submissionTabFiles).hasSize(3)
        assertThat(submissionTabFiles).isEqualTo(submissionFireTabFiles(accNo, subFolder))

        val fileListTabFiles = submission.section.fileList!!.pageTabFiles as List<FireFile>
        assertThat(fileListTabFiles).hasSize(3)
        assertThat(fileListTabFiles).isEqualTo(fireTabFiles(subFolder, "file-list"))

        val subFileListTabFiles =
            (submission.section.sections.first() as Either.Left).a.fileList!!.pageTabFiles as List<FireFile>
        assertThat(subFileListTabFiles).hasSize(3)
        assertThat(subFileListTabFiles).isEqualTo(fireTabFiles(subFolder, "sub-folder/file-list2"))
    }

    private fun submissionFireTabFiles(accNo: String, subFolder: String): List<FireFile> {
        val jsonName = "$accNo.json"
        val xmlName = "$accNo.xml"
        val tsvName = "$accNo.pagetab.tsv"
        val json = File("$subFolder/$accNo.json")
        val xml = File("$subFolder/$accNo.xml")
        val tsv = File("$subFolder/$accNo.pagetab.tsv")
        return listOf(
            FireFile(jsonName, jsonName, "fireOid-$jsonName", json.md5(), json.size(), listOf()),
            FireFile(xmlName, xmlName, "fireOid-$xmlName", xml.md5(), xml.size(), listOf()),
            FireFile(tsvName, tsvName, "fireOid-$tsvName", tsv.md5(), tsv.size(), listOf())
        )
    }

    private fun fireTabFiles(subFolder: String, list: String): List<FireFile> {
        val name = list.substringAfterLast("/")
        val path = "Files/$list"
        val json = File("$subFolder/$path.json")
        val xml = File("$subFolder/$path.xml")
        val tsv = File("$subFolder/$path.pagetab.tsv")
        val TSV = "pagetab.tsv"
        return listOf(
            FireFile("$list.json", "$path.json", "fireOid-$name.json", json.md5(), json.size(), listOf()),
            FireFile("$list.xml", "$path.xml", "fireOid-$name.xml", xml.md5(), xml.size(), listOf()),
            FireFile("$list.$TSV", "$path.$TSV", "fireOid-$name.$TSV", tsv.md5(), tsv.size(), listOf())
        )
    }

    private fun submissionNfsTabFiles(accNo: String, submissionFolderPath: String): List<NfsFile> {
        val jsonFile = File("$submissionFolderPath/$accNo.json")
        val xmlFile = File("$submissionFolderPath/$accNo.xml")
        val tsvFile = File("$submissionFolderPath/$accNo.pagetab.tsv")

        return listOf(
            NfsFile("$accNo.json", "$accNo.json", jsonFile.absolutePath, jsonFile),
            NfsFile("$accNo.xml", "$accNo.xml", xmlFile.absolutePath, xmlFile),
            NfsFile("$accNo.pagetab.tsv", "$accNo.pagetab.tsv", tsvFile.absolutePath, tsvFile)
        )
    }

    private fun nfsTabFiles(subFolder: String, list: String): List<NfsFile> {
        val path = "Files/$list"
        val json = File("$subFolder/$path.json")
        val xml = File("$subFolder/$path.xml")
        val tsv = File("$subFolder/$path.pagetab.tsv")
        return listOf(
            NfsFile("$list.json", "$path.json", json.absolutePath, json),
            NfsFile("$list.xml", "$path.xml", xml.absolutePath, xml),
            NfsFile("$list.pagetab.tsv", "$path.pagetab.tsv", tsv.absolutePath, tsv)
        )
    }

    private fun getSubFileContent(path: String): String {
        val filePath = Paths.get(path)
        assertThat(filePath).exists()

        return filePath.toFile().readText()
    }
}
