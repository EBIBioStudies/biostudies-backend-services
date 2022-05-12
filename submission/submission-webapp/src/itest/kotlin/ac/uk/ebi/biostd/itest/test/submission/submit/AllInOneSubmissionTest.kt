package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.XML
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
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
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

@ExtendWith(TemporaryFolderExtension::class)
internal class AllInOneSubmissionTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SingleSubmissionTest(
        @Autowired val subRepository: SubmissionQueryService,
        @Autowired val securityTestService: SecurityTestService,
        @Autowired val toSubmissionMapper: ToSubmissionMapper
    ) {
        @LocalServerPort
        private var serverPort: Int = 0
        private lateinit var webClient: BioWebClient
        private lateinit var allInOneSubmissionHelper: AllInOneSubmissionHelper

        @BeforeAll
        fun init() {
            securityTestService.registerUser(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
            allInOneSubmissionHelper = AllInOneSubmissionHelper(submissionPath, subRepository, toSubmissionMapper)
        }

        @Test
        fun `submit all in one TSV submission`() {
            val (submission, fileList, files, subFileList) = submissionSpecTsv(tempFolder, "S-EPMC124")
            webClient.uploadFile(fileList)
            subFileList?.let { webClient.uploadFile(it.file, it.folder) }
            files.forEach { webClient.uploadFile(it.file, it.folder) }

            webClient.submitSingle(submission.readText(), TSV)

            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC124")
            if (enableFire) allInOneSubmissionHelper.assertSubmissionFilesRecordsFire("S-EPMC124")
            else allInOneSubmissionHelper.assertSubmissionFilesRecordsNfs("S-EPMC124")
        }

        @Test
        fun `submit all in one JSON submission`() {
            val (submission, fileList, files, subFileList) = submissionSpecJson(tempFolder, "S-EPMC125")
            webClient.uploadFile(fileList)
            subFileList?.let { webClient.uploadFile(it.file, it.folder) }
            files.forEach { webClient.uploadFile(it.file, it.folder) }

            webClient.submitSingle(submission.readText(), JSON)

            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC125")
            if (enableFire) allInOneSubmissionHelper.assertSubmissionFilesRecordsFire("S-EPMC125")
            else allInOneSubmissionHelper.assertSubmissionFilesRecordsNfs("S-EPMC125")
        }

        @Test
        fun `submit all in one XML submission`() {
            val (submission, fileList, files, subFileList) = submissionSpecXml(tempFolder, "S-EPMC126")
            webClient.uploadFile(fileList)
            subFileList?.let { webClient.uploadFile(it.file, it.folder) }
            files.forEach { webClient.uploadFile(it.file, it.folder) }

            webClient.submitSingle(submission.readText(), XML)

            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC126")
            if (enableFire) allInOneSubmissionHelper.assertSubmissionFilesRecordsFire("S-EPMC126")
            else allInOneSubmissionHelper.assertSubmissionFilesRecordsNfs("S-EPMC126")
        }
    }
}
