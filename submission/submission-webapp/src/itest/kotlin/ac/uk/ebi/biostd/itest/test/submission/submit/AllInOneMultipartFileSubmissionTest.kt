package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.assertions.AllInOneSubmissionHelper
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.factory.submissionSpecJson
import ac.uk.ebi.biostd.itest.factory.submissionSpecTsv
import ac.uk.ebi.biostd.itest.factory.submissionSpecXml
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ebi.ac.uk.extended.model.ExtSubmissionMethod.FILE
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ExtendWith(TemporaryFolderExtension::class)
internal class AllInOneMultipartFileSubmissionTest(
    private val tempFolder: TemporaryFolder
) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(PersistenceConfig::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Transactional
    @DirtiesContext
    inner class SingleSubmissionTest(
        @Autowired var securityTestService: SecurityTestService,
        @Autowired val submissionRepository: SubmissionQueryService
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient
        private lateinit var allInOneSubmissionHelper: AllInOneSubmissionHelper

        @BeforeAll
        fun init() {
            securityTestService.registerUser(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
            allInOneSubmissionHelper = AllInOneSubmissionHelper(submissionPath, submissionRepository)
        }

        @Test
        fun `submit all in one multipart TSV submission`() {
            val (submission, fileList, files, subFileList) = submissionSpecTsv(tempFolder, "S-EPMC124")
            webClient.uploadFile(fileList)
            subFileList?.let { webClient.uploadFile(it.file, it.folder) }
            files.forEach { webClient.uploadFile(it.file, it.folder) }

            webClient.submitSingle(submission, emptyList())

            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC124", method = FILE)
            if (mongoMode)
                if (enableFire) allInOneSubmissionHelper.assertSubmissionFilesRecordsFire("S-EPMC124")
                else allInOneSubmissionHelper.assertSubmissionFilesRecordsNfs("S-EPMC124")
        }

        @Test
        fun `submit all in one multipart Json submission`() {
            val (submission, fileList, files, subFileList) = submissionSpecJson(tempFolder, "S-EPMC125")
            webClient.uploadFile(fileList)
            subFileList?.let { webClient.uploadFile(it.file, it.folder) }
            files.forEach { webClient.uploadFile(it.file, it.folder) }

            webClient.submitSingle(submission, emptyList())

            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC125", method = FILE)
            if (mongoMode)
                if (enableFire) allInOneSubmissionHelper.assertSubmissionFilesRecordsFire("S-EPMC125")
                else allInOneSubmissionHelper.assertSubmissionFilesRecordsNfs("S-EPMC125")
        }

        @Test
        fun `submit all in one multipart XML submission`() {
            val (submission, fileList, files, subFileList) = submissionSpecXml(tempFolder, "S-EPMC126")
            webClient.uploadFile(fileList)
            subFileList?.let { webClient.uploadFile(it.file, it.folder) }
            files.forEach { webClient.uploadFile(it.file, it.folder) }

            webClient.submitSingle(submission, emptyList())

            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC126", method = FILE)
            if (mongoMode)
                if (enableFire) allInOneSubmissionHelper.assertSubmissionFilesRecordsFire("S-EPMC126")
                else allInOneSubmissionHelper.assertSubmissionFilesRecordsNfs("S-EPMC126")
        }
    }
}
