package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.OffsetDateTime

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProjectSubmitTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val tagsDataRepository: AccessTagDataRepo,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired val sequenceRepository: SequenceDataRepository,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runTest {
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

    @Test
    fun `7-1 submit private project`() =
        runTest {
            val privateProject =
                tsv {
                    line("Submission", "PrivateProject")
                    line("Title", "A Private Project")
                    line("AccNoTemplate", "!{S-PRP}")
                    line()

                    line("Project")
                }.toString()

            assertThat(webClient.submit(privateProject, TSV)).isSuccessful()

            val submittedProject = submissionRepository.getExtByAccNo("PrivateProject")
            assertThat(submittedProject.accNo).isEqualTo("PrivateProject")
            assertThat(submittedProject.title).isEqualTo("A Private Project")

            assertThat(submittedProject.collections).hasSize(1)
            assertThat(submittedProject.collections.first().accNo).isEqualTo("PrivateProject")

            assertThat(tagsDataRepository.existsByName("PrivateProject")).isTrue
            assertThat(sequenceRepository.existsByPrefix("S-PRP")).isTrue
        }

    @Test
    fun `7-2 submit public project`() =
        runTest {
            val publicProject =
                tsv {
                    line("Submission", "PublicProject")
                    line("Title", "Public Project")
                    line("AccNoTemplate", "!{S-PUB-EXT}")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()

                    line("Project")
                }.toString()

            assertThat(webClient.submit(publicProject, TSV)).isSuccessful()

            val submittedProject = submissionRepository.getExtByAccNo("PublicProject")
            assertThat(submittedProject.accNo).isEqualTo("PublicProject")
            assertThat(submittedProject.title).isEqualTo("Public Project")
            assertThat(submittedProject.collections).containsExactly(ExtCollection("PublicProject"))
            assertThat(tagsDataRepository.existsByName("PublicProject")).isTrue
            assertThat(sequenceRepository.existsByPrefix("S-PUB-EXT")).isTrue
        }

    @Test
    fun `7-3 submit duplicated accNo template`() {
        val aProject =
            tsv {
                line("Submission", "A-Project")
                line("AccNoTemplate", "!{S-APRJ}")
                line()

                line("Project")
            }.toString()

        val anotherProject =
            tsv {
                line("Submission", "Another-Project")
                line("AccNoTemplate", "!{S-APRJ}")
                line()

                line("Project")
            }.toString()

        assertThat(webClient.submit(aProject, TSV)).isSuccessful()
        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { webClient.submit(anotherProject, TSV) }
            .withMessageContaining("There is a collection already using the accNo template 'S-APRJ'")
    }
}
