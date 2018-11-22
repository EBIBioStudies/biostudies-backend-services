package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.config.PersistenceConfig
import ac.uk.ebi.biostd.config.SubmitterConfig
import ac.uk.ebi.biostd.itest.common.setAppProperty
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionJson
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constans.SubFields
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.security.integration.model.SignUpRequest
import ebi.ac.uk.security.service.SecurityService
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(TemporaryFolderExtension::class)
@TestInstance(PER_CLASS)
class SubmissionTest(private val temporaryFolder: TemporaryFolder) {
    @BeforeAll
    fun init() {
        setAppProperty("{BASE_PATH}", temporaryFolder.root.absolutePath)
    }

    @Nested
    @TestInstance(PER_CLASS)
    @ExtendWith(SpringExtension::class)
    @Import(value = [SubmitterConfig::class, PersistenceConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    inner class SimpleSubmission {

        @LocalServerPort
        private var randomServerPort: Int = 0

        @Autowired
        private lateinit var submissionRepository: SubmissionRepository

        @Autowired
        private lateinit var securityService: SecurityService

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            // TODO: teardown properly
            securityService.registerUser(SignUpRequest("test@biostudies.com", "jhon_doe", "12345"))
            webClient = BioWebClient.create(
                baseUrl = "http://localhost:$randomServerPort",
                token = securityService.login("jhon_doe", "12345"))
        }

        @Test
        fun `submit simple submission`() {
            val accNo = "SimpleAcc1"
            val title = "Simple Submission"
            val submission = Submission(accNo = accNo)
            submission[SubFields.TITLE] = title

            val response = webClient.submitSingle(submission, SubmissionFormat.XML)

            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

            val savedSubmission = submissionRepository.findByAccNo(accNo)
            assertThat(savedSubmission).isNotNull
            assertThat(savedSubmission.accNo).isEqualTo(accNo)
            assertThat(savedSubmission.title).isEqualTo(title)
        }

        @Test
        fun `submit all in one submission`() {
            val response = webClient.submitSingle(allInOneSubmissionJson().toString())
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

            val savedSubmission = submissionRepository.findByAccNo("S-EPMC124")
            assertThat(savedSubmission).isNotNull
        }
    }
}
