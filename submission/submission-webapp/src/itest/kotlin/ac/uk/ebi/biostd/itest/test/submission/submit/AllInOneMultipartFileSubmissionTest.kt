package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.XML
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.assertions.AllInOneSubmissionHelper
import ac.uk.ebi.biostd.itest.assertions.submitAllInOneMultipartSubmission
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.extended.model.ExtSubmissionMethod
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
        @Autowired val submissionRepository: SubmissionRepository
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
        fun `submit multipart all in one TSV`() {
            webClient.submitAllInOneMultipartSubmission("S-EPMC124", TSV, tempFolder)
            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC124", ExtSubmissionMethod.FILE)
        }

        @Test
        fun `submit multipart all in one JSON`() {
            webClient.submitAllInOneMultipartSubmission("S-EPMC125", JSON, tempFolder)
            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC125", ExtSubmissionMethod.FILE)
        }

        @Test
        fun `submit multipart all in one XML`() {
            webClient.submitAllInOneMultipartSubmission("S-EPMC126", XML, tempFolder)
            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC126", ExtSubmissionMethod.FILE)
        }
    }
}
