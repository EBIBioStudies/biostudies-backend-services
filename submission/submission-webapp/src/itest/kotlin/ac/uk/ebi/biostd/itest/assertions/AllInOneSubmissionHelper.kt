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
import org.assertj.core.api.Assertions.assertThat
import java.io.File
import java.nio.file.Paths

internal class AllInOneSubmissionHelper(
    private val submissionPath: String,
    private val submissionRepository: SubmissionQueryService
) {
    internal fun assertSavedSubmission(accNo: String, method: ExtSubmissionMethod = ExtSubmissionMethod.PAGE_TAB) {
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

    private fun getSubFileContent(path: String): String {
        val filePath = Paths.get(path)
        assertThat(filePath).exists()

        return filePath.toFile().readText()
    }
}
