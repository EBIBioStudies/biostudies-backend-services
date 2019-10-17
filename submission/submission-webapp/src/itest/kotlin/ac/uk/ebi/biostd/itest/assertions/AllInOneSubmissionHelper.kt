package ac.uk.ebi.biostd.itest.assertions

import ac.uk.ebi.biostd.itest.factory.allInOneSubmission
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionXml
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.model.ExtendedSubmission
import org.assertj.core.api.Assertions.assertThat
import java.nio.file.Paths

internal class AllInOneSubmissionHelper(
    private val basePath: String,
    private val submissionRepository: SubmissionRepository
) {
    internal fun assertSavedSubmission(accNo: String) {
        val extendedSubmission = submissionRepository.getExtendedByAccNo(accNo)
        assertThat(extendedSubmission.asSubmission()).isEqualTo(allInOneSubmission(accNo))
        assertSubmissionFiles(extendedSubmission)
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
