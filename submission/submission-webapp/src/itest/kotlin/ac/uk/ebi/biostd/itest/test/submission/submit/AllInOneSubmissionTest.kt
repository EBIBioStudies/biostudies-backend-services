package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.assertions.SubmissionAssertHelper
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.TestConfig
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionXml
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(TemporaryFolderExtension::class)
internal class AllInOneSubmissionTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [TestConfig::class, PersistenceConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SingleSubmissionTest(@Autowired val submissionRepository: SubmissionRepository) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        private lateinit var assertHelper: SubmissionAssertHelper

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(SuperUser.asRegisterRequest())
            webClient = securityClient.getAuthenticatedClient(SuperUser.email, SuperUser.password)
            assertHelper = SubmissionAssertHelper(basePath)

            tempFolder.createDirectory("Folder1")
            tempFolder.createDirectory("Folder1/Folder2")

            webClient.uploadFiles(listOf(tempFolder.createFile("DataFile1.txt"), tempFolder.createFile("DataFile2.txt")))
            webClient.uploadFiles(listOf(tempFolder.createFile("Folder1/DataFile3.txt")), "Folder1")
            webClient.uploadFiles(listOf(tempFolder.createFile("Folder1/Folder2/DataFile4.txt")), "Folder1/Folder2")
        }

        @Test
        fun `submit all in one TSV submission`() {
            val accNo = "S-EPMC124"

            val response = webClient.submitSingle(allInOneSubmissionTsv(accNo).toString(), SubmissionFormat.TSV)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertHelper.assertSavedSubmission(accNo, submissionRepository.getExtendedByAccNo(accNo))
        }

        @Test
        fun `submit all in one JSON submission`() {
            val accNo = "S-EPMC125"

            val response = webClient.submitSingle(allInOneSubmissionJson(accNo).toString(), SubmissionFormat.JSON)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertHelper.assertSavedSubmission(accNo, submissionRepository.getExtendedByAccNo(accNo))
        }

        @Test
        fun `submit all in one XML submission`() {
            val accNo = "S-EPMC126"

            val response = webClient.submitSingle(allInOneSubmissionXml(accNo).toString(), SubmissionFormat.XML)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertHelper.assertSavedSubmission(accNo, submissionRepository.getExtendedByAccNo(accNo))
        }
    }
}
