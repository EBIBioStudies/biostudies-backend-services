package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
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
import org.springframework.web.client.HttpServerErrorException

@ExtendWith(TemporaryFolderExtension::class)
internal class DeletePermissionTest(tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class DeleteSubmissionTest(@Autowired private val submissionRepository: SubmissionRepository) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var superUserWebClient: BioWebClient
        private lateinit var regularUserWebClient: BioWebClient

        @BeforeAll
        fun init() {
            superUserWebClient = getWebClient(serverPort, SuperUser)
            regularUserWebClient = getWebClient(serverPort, RegularUser)
        }

        @Test
        fun `submit and delete submission`() {
            val submission = tsv {
                line("Submission", "SimpleAcc1")
                line("Title", "Simple Submission")
                line()
            }.toString()

            submitString(superUserWebClient, submission)
            superUserWebClient.deleteSubmission("SimpleAcc1")

            val deletedSubmission = submissionRepository.getExtendedLastVersionByAccNo("SimpleAcc1")
            assertThat(deletedSubmission.version).isEqualTo(-1)
        }

        @Test
        fun `submit with one user and delete with another`() {
            val submission = tsv {
                line("Submission", "SimpleAcc2")
                line("Title", "Simple Submission")
                line()
            }.toString()

            submitString(superUserWebClient, submission)

            assertThatExceptionOfType(HttpServerErrorException::class.java).isThrownBy {
                regularUserWebClient.deleteSubmission("SimpleAcc2")
            }
        }
    }
}
