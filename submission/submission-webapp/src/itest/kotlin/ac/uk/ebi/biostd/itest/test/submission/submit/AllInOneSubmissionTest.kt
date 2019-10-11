package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.assertions.AllInOneSubmissionHelper
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionXml
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
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
    inner class SingleSubmissionTest(@Autowired val submissionRepository: SubmissionRepository) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient
        private lateinit var allInOneSubmissionHelper: AllInOneSubmissionHelper

        @BeforeAll
        fun init() {
            webClient = getWebClient(serverPort, SuperUser)
            allInOneSubmissionHelper = AllInOneSubmissionHelper(basePath, submissionRepository)
            allInOneSubmissionHelper.createAllInOneSubmissionFiles(webClient, tempFolder)
        }

        @Test
        fun `submit all in one TSV submission`() {
            submitString(webClient, allInOneSubmissionTsv("S-EPMC124").toString(), SubmissionFormat.TSV)
            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC124")
        }

        @Test
        fun `submit all in one JSON submission`() {
            submitString(webClient, allInOneSubmissionJson("S-EPMC125").toString(), SubmissionFormat.JSON)
            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC125")
        }

        @Test
        fun `submit all in one XML submission`() {
            submitString(webClient, allInOneSubmissionXml("S-EPMC126").toString(), SubmissionFormat.XML)
            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC126")
        }
    }
}
