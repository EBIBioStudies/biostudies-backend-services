package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.XML
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.assertions.AllInOneSubmissionHelper
import ac.uk.ebi.biostd.itest.assertions.submitAllInOneSubmission
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
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
        @Autowired val submissionRepository: SubmissionQueryService,
        @Autowired val securityTestService: SecurityTestService
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
        fun `submit all in one TSV submission`() {
            webClient.submitAllInOneSubmission("S-EPMC124", TSV, tempFolder)
            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC124")
        }

        @Test
        fun `submit all in one JSON submission`() {
            webClient.submitAllInOneSubmission("S-EPMC125", JSON, tempFolder)
            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC125")
        }

        @Test
        fun `submit all in one XML submission`() {
            webClient.submitAllInOneSubmission("S-EPMC126", XML, tempFolder)
            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC126")
        }
    }
}
