package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.assertions.AllInOneSubmissionHelper
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionXml
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.test.createFile
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
        fun `submit multipart all in one TSV`() {
            val tsvSubmission = tempFolder.createFile("S-EPMC124.tsv", allInOneSubmissionTsv("S-EPMC124").toString())
            submitFile(webClient, tsvSubmission)
            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC124")
        }

        @Test
        fun `submit multipart all in one JSON`() {
            val jsonSubmission = tempFolder.createFile("S-EPMC125.json", allInOneSubmissionJson("S-EPMC125").toString())
            submitFile(webClient, jsonSubmission)
            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC125")
        }

        @Test
        fun `submit multipart all in one XML`() {
            val xmlSubmission = tempFolder.createFile("S-EPMC126.xml", allInOneSubmissionXml("S-EPMC126").toString())
            submitFile(webClient, xmlSubmission)
            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC126")
        }
    }
}
