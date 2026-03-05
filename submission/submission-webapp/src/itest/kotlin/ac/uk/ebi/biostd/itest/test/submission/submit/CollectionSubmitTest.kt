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
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
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
class CollectionSubmitTest(
    @param:Autowired val securityTestService: SecurityTestService,
    @param:Autowired val tagsDataRepository: AccessTagDataRepo,
    @param:Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @param:Autowired val sequenceRepository: SequenceDataRepository,
    @param:LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runTest {
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

    @Test
    fun `7-1 submit private collection`() =
        runTest {
            val privateCollection =
                tsv {
                    line("Submission", "PrivateProject")
                    line("Title", "A Private Project")
                    line("AccNoTemplate", "!{S-PRP}")
                    line("ReleaseDate", "2099-09-21")
                    line()

                    line("Project")
                }.toString()

            assertThat(webClient.submit(privateCollection, TSV)).isSuccessful()

            val submittedCollection = submissionRepository.getExtByAccNo("PrivateProject")
            assertThat(submittedCollection.accNo).isEqualTo("PrivateProject")
            assertThat(submittedCollection.title).isEqualTo("A Private Project")

            assertThat(submittedCollection.collections).hasSize(1)
            assertThat(submittedCollection.collections.first().accNo).isEqualTo("PrivateProject")

            assertThat(tagsDataRepository.existsByName("PrivateProject")).isTrue
            assertThat(sequenceRepository.existsByPrefix("S-PRP")).isTrue
        }

    @Test
    fun `7-2 submit public collection`() =
        runTest {
            val publicCollection =
                tsv {
                    line("Submission", "PublicProject")
                    line("Title", "Public Project")
                    line("AccNoTemplate", "!{S-PUB-EXT}")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()

                    line("Project")
                }.toString()

            assertThat(webClient.submit(publicCollection, TSV)).isSuccessful()

            val submittedCollection = submissionRepository.getExtByAccNo("PublicProject")
            assertThat(submittedCollection.accNo).isEqualTo("PublicProject")
            assertThat(submittedCollection.title).isEqualTo("Public Project")
            assertThat(submittedCollection.collections).containsExactly(ExtCollection("PublicProject"))
            assertThat(tagsDataRepository.existsByName("PublicProject")).isTrue
            assertThat(sequenceRepository.existsByPrefix("S-PUB-EXT")).isTrue
        }

    @Test
    fun `7-3 submit duplicated accNo template`() =
        runTest {
            val aCollection =
                tsv {
                    line("Submission", "A-Project")
                    line("AccNoTemplate", "!{S-APRJ}")
                    line("ReleaseDate", "2099-09-21")
                    line()

                    line("Project")
                }.toString()

            val anotherCollection =
                tsv {
                    line("Submission", "Another-Project")
                    line("AccNoTemplate", "!{S-APRJ}")
                    line("ReleaseDate", "2099-09-21")
                    line()

                    line("Project")
                }.toString()

            assertThat(webClient.submit(aCollection, TSV)).isSuccessful()
            val exception = assertThrows<WebClientException> { webClient.submit(anotherCollection, TSV) }
            assertThat(exception).hasMessageContaining("There is a collection already using the accNo template 'S-APRJ'")
        }
}
