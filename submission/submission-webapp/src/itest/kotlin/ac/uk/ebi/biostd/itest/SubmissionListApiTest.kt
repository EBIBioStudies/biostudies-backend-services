package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.TestConfig
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.title
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
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionListApiTest(tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [TestConfig::class, SubmitterConfig::class, PersistenceConfig::class, TestConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SingleSubmissionTest(@Autowired val tagsDataRepository: TagsDataRepository) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(SuperUser.asRegisterRequest())
            securityClient.registerUser(RegularUser.asRegisterRequest())
            tagsDataRepository.save(AccessTag(name = "Public"))

            webClient = securityClient.getAuthenticatedClient(SuperUser.email, SuperUser.password)
            for (idx in 11..30) {
                val submission = submission("SimpleAcc$idx") {
                    title = "Simple Submission $idx - keyword$idx"
                    releaseDate = "2019-09-$idx"
                }

                webClient.submitSingle(submission, SubmissionFormat.JSON)
            }
        }

        @Test
        fun `get submission list`() {
            val submissionList = webClient.getSubmissions()

            assertThat(submissionList).isNotNull
            assertThat(submissionList).hasSize(15)
            assertThat(submissionList).isSortedAccordingTo { a, b -> b.rtime.compareTo(a.rtime) }
        }

        @Test
        fun `get submission list by accession`() {
            val submissionList = webClient.getSubmissions(mapOf(
                "accNo" to "SimpleAcc17"
            ))

            assertThat(submissionList).hasOnlyOneElementSatisfying {
                assertThat(it.accno).isEqualTo("SimpleAcc17")
                assertThat(it.version).isEqualTo(1)
            }
        }

        @Test
        fun `get submission list by keywords`() {
            val submissionList = webClient.getSubmissions(mapOf(
                "keywords" to "keyword20"
            ))

            assertThat(submissionList).hasOnlyOneElementSatisfying {
                assertThat(it.title).contains("keyword20")
            }
        }

        @Test
        fun `get submission list by release date`() {
            val submissionList = webClient.getSubmissions(mapOf(
                "rTimeFrom" to "2019-09-24T09:41:44.000Z",
                "rTimeTo" to "2019-09-28T09:41:44.000Z"
            ))

            assertThat(submissionList).hasSize(4)
        }

        @Test
        fun `get submission list pagination`() {
            val submissionList = webClient.getSubmissions(mapOf(
                "offset" to 15
            ))

            assertThat(submissionList).hasSize(5)
        }
    }
}
