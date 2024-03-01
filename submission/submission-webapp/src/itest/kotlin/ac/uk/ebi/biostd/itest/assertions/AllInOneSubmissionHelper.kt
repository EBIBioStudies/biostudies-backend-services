package ac.uk.ebi.biostd.itest.assertions

import ac.uk.ebi.biostd.itest.factory.allInOneSubmission
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.expectedAllInOneJsonFileList
import ac.uk.ebi.biostd.itest.factory.expectedAllInOneJsonInnerFileList
import ac.uk.ebi.biostd.itest.factory.expectedAllInOneTsvFileList
import ac.uk.ebi.biostd.itest.factory.expectedAllInOneTsvInnerFileList
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import arrow.core.Either
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import java.io.File
import java.nio.file.Paths

internal class AllInOneSubmissionHelper(
    private val submissionPath: File,
    private val submissionRepository: SubmissionPersistenceQueryService,
    private val toSubmissionMapper: ToSubmissionMapper,
) {
    internal suspend fun assertSavedSubmission(
        accNo: String,
        method: ExtSubmissionMethod = ExtSubmissionMethod.PAGE_TAB,
    ) {
        val extendedSubmission = submissionRepository.getExtByAccNo(accNo)

        assertThat(extendedSubmission.method).isEqualTo(method)
        assertThat(toSubmissionMapper.toSimpleSubmission(extendedSubmission)).isEqualTo(allInOneSubmission(accNo))
        assertSubmissionFiles(extendedSubmission)
    }

    private fun assertSubmissionFiles(submission: ExtSubmission) {
        val submissionFolderPath = submissionPath.resolve(submission.relPath)
        val accNo = submission.accNo

        assertAllInOneSubmissionJson(getSubFileContent("$submissionFolderPath/$accNo.json"), accNo)
        assertAllInOneSubmissionTsv(getSubFileContent("$submissionFolderPath/$accNo.tsv"), accNo)
    }

    suspend fun assertNfsPagetabFiles(accNo: String) {
        val submission = submissionRepository.getExtByAccNo(accNo)
        val subFolder = "$submissionPath/${submission.relPath}"

        val submissionTabFiles = submission.pageTabFiles
        assertThat(submissionTabFiles).hasSize(2)
        assertThat(submissionTabFiles).isEqualTo(submissionNfsTabFiles(accNo, subFolder))

        val fileListTabFiles = submission.section.fileList!!.pageTabFiles
        assertThat(fileListTabFiles).hasSize(2)
        assertThat(fileListTabFiles).isEqualTo(nfsTabFiles(subFolder, "file-list"))

        val subFileListTabFiles = (submission.section.sections.first() as Either.Left).a.fileList!!.pageTabFiles
        assertThat(subFileListTabFiles).hasSize(2)
        assertThat(subFileListTabFiles).isEqualTo(nfsTabFiles(subFolder, "sub-folder/file-list2"))

        assertFileListsPagetabFiles(subFolder)
    }

    suspend fun assertFirePagetabFiles(accNo: String) {
        val submission = submissionRepository.getExtByAccNo(accNo)
        val subFolder = "$submissionPath/${submission.relPath}"

        assertFireTabFiles(submission, accNo, subFolder)
        assertFireFileListTabFiles(submission, subFolder)
        assertFireSubFileListTabFiles(submission, subFolder)
        assertFileListsPagetabFiles(subFolder)
    }

    private fun assertFireTabFiles(submission: ExtSubmission, accNo: String, subFolder: String) {
        val submissionTabFiles = submission.pageTabFiles
        assertThat(submissionTabFiles).hasSize(2)

        val jsonTabFile = submissionTabFiles.first() as FireFile
        val jsonFile = File("$subFolder/$accNo.json")
        assertThat(jsonTabFile.filePath).isEqualTo("$accNo.json")
        assertThat(jsonTabFile.relPath).isEqualTo("$accNo.json")
        assertThat(jsonTabFile.fireId).endsWith("_$accNo.json")
        assertThat(jsonTabFile.md5).isEqualTo(jsonFile.md5())
        assertThat(jsonTabFile.size).isEqualTo(jsonFile.size())

        val tsvTabFile = submissionTabFiles.second() as FireFile
        val tsvFile = File("$subFolder/$accNo.tsv")
        assertThat(tsvTabFile.filePath).isEqualTo("$accNo.tsv")
        assertThat(tsvTabFile.relPath).isEqualTo("$accNo.tsv")
        assertThat(tsvTabFile.fireId).endsWith("_$accNo.tsv")
        assertThat(tsvTabFile.md5).isEqualTo(tsvFile.md5())
        assertThat(tsvTabFile.size).isEqualTo(tsvFile.size())
    }

    private fun assertFireFileListTabFiles(submission: ExtSubmission, subFolder: String) {
        val fileListTabFiles = submission.section.fileList!!.pageTabFiles
        assertThat(fileListTabFiles).hasSize(2)

        val jsonTabFile = fileListTabFiles.first() as FireFile
        val jsonFile = File("$subFolder/Files/file-list.json")
        assertThat(jsonTabFile.filePath).isEqualTo("file-list.json")
        assertThat(jsonTabFile.relPath).isEqualTo("Files/file-list.json")
        assertThat(jsonTabFile.fireId).endsWith("file-list.json")
        assertThat(jsonTabFile.md5).isEqualTo(jsonFile.md5())
        assertThat(jsonTabFile.size).isEqualTo(jsonFile.size())

        val tsvTabFile = fileListTabFiles.second() as FireFile
        val tsvFile = File("$subFolder/Files/file-list.tsv")
        assertThat(tsvTabFile.filePath).isEqualTo("file-list.tsv")
        assertThat(tsvTabFile.relPath).isEqualTo("Files/file-list.tsv")
        assertThat(tsvTabFile.fireId).endsWith("_file-list.tsv")
        assertThat(tsvTabFile.md5).isEqualTo(tsvFile.md5())
        assertThat(tsvTabFile.size).isEqualTo(tsvFile.size())
    }

    private fun assertFireSubFileListTabFiles(submission: ExtSubmission, subFolder: String) {
        val subFileListTabFiles = (submission.section.sections.first() as Either.Left).a.fileList!!.pageTabFiles
        assertThat(subFileListTabFiles).hasSize(2)

        val jsonTabFile = subFileListTabFiles.first() as FireFile
        val jsonFile = File("$subFolder/Files/sub-folder/file-list2.json")
        assertThat(jsonTabFile.filePath).isEqualTo("sub-folder/file-list2.json")
        assertThat(jsonTabFile.relPath).isEqualTo("Files/sub-folder/file-list2.json")
        assertThat(jsonTabFile.fireId).endsWith("_file-list2.json")
        assertThat(jsonTabFile.md5).isEqualTo(jsonFile.md5())
        assertThat(jsonTabFile.size).isEqualTo(jsonFile.size())

        val tsvTabFile = subFileListTabFiles.second() as FireFile
        val tsvFile = File("$subFolder/Files/sub-folder/file-list2.tsv")
        assertThat(tsvTabFile.filePath).isEqualTo("sub-folder/file-list2.tsv")
        assertThat(tsvTabFile.relPath).isEqualTo("Files/sub-folder/file-list2.tsv")
        assertThat(tsvTabFile.fireId).endsWith("_file-list2.tsv")
        assertThat(tsvTabFile.md5).isEqualTo(tsvFile.md5())
        assertThat(tsvTabFile.size).isEqualTo(tsvFile.size())
    }

    private fun assertFileListsPagetabFiles(subFolder: String) {
        val tsvFile = getSubFileContent("$subFolder/Files/file-list.tsv")
        val tsvInnerFile = getSubFileContent("$subFolder/Files/sub-folder/file-list2.tsv")
        assertThat(tsvFile).isEqualToIgnoringWhitespace(expectedAllInOneTsvFileList.toString())
        assertThat(tsvInnerFile).isEqualToIgnoringWhitespace(expectedAllInOneTsvInnerFileList.toString())

        val jsonFile = getSubFileContent("$subFolder/Files/file-list.json")
        val jsonInnerFile = getSubFileContent("$subFolder/Files/sub-folder/file-list2.json")
        assertThat(jsonFile).isEqualToIgnoringWhitespace(expectedAllInOneJsonFileList.toString())
        assertThat(jsonInnerFile).isEqualToIgnoringWhitespace(expectedAllInOneJsonInnerFileList.toString())
    }

    private fun submissionNfsTabFiles(accNo: String, submissionFolderPath: String): List<NfsFile> {
        return listOf(
            createNfsFile("$accNo.json", "$accNo.json", File("$submissionFolderPath/$accNo.json")),
            createNfsFile("$accNo.tsv", "$accNo.tsv", File("$submissionFolderPath/$accNo.tsv"))
        )
    }

    private fun nfsTabFiles(subFolder: String, list: String): List<NfsFile> {
        val path = "Files/$list"
        val json = File("$subFolder/$path.json")
        val tsv = File("$subFolder/$path.tsv")

        return listOf(
            createNfsFile("$list.json", "$path.json", json),
            createNfsFile("$list.tsv", "$path.tsv", tsv)
        )
    }

    private fun getSubFileContent(path: String): String {
        val filePath = Paths.get(path)
        assertThat(filePath).exists()

        return filePath.toFile().readText()
    }
}
