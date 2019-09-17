package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.TestConfig
import ac.uk.ebi.biostd.itest.entities.GenericUser
import ac.uk.ebi.biostd.persistence.model.Tag
import ac.uk.ebi.biostd.persistence.repositories.TagsRefRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields
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
internal class SubmissionApiTest(tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [TestConfig::class, SubmitterConfig::class, PersistenceConfig::class, TestConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SingleSubmissionTest(
        @Autowired val submissionRepository: SubmissionRepository,
        @Autowired val tagsRefRepository: TagsRefRepository
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(GenericUser.asRegisterRequest())
            webClient = securityClient.getAuthenticatedClient(GenericUser.email, GenericUser.password)
            tagsRefRepository.save(Tag(classifier = "classifier", name = "tag"))
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

            val savedSubmission = submissionRepository.getByAccNo(accNo)
            assertThat(savedSubmission).isNotNull
            assertThat(savedSubmission).isEqualTo(submission)
        }

        @Test
        fun `submit and delete submission`() {
            val accNo = "SimpleAcc2"
            val title = "Simple Submission"
            val submission = Submission(accNo = accNo)
            submission[SubFields.TITLE] = title

            webClient.submitSingle(submission, SubmissionFormat.JSON)
            webClient.deleteSubmission(submission.accNo)

            val storeSubmission = submissionRepository.getExtendedLastVersionByAccNo(accNo)
            assertThat(storeSubmission.version).isEqualTo(-1)
        }

        @Test
        fun `submission with tags`() {
            val accNo = "SimpleAcc3"
            val title = "Simple Submission With Tags"
            val submission = Submission(accNo = accNo)

            submission[SubFields.TITLE] = title
            submission.tags.add(Pair("classifier", "tag"))

            val response = webClient.submitSingle(submission, SubmissionFormat.JSON)

            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

            val savedSubmission = submissionRepository.getByAccNo(accNo)
            assertThat(savedSubmission).isNotNull
            assertThat(savedSubmission).isEqualTo(submission)
        }
    }
}
