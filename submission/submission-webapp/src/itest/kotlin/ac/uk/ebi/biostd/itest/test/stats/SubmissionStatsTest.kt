package ac.uk.ebi.biostd.itest.test.stats

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.DIRECTORIES
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.NON_DECLARED_FILES_DIRECTORIES
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.VIEWS
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.model.SubmissionStat
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Durations.TEN_SECONDS
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionStatsTest(
    @param:Autowired val statsDataRepository: SubmissionStatsDataRepository,
    @param:Autowired val securityTestService: SecurityTestService,
    @param:Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @param:LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            securityTestService.ensureUserRegistration(RegularUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

    @AfterEach
    fun afterEach() =
        runBlocking {
            statsDataRepository.deleteAll()
        }

    @Test
    fun `26-1 find stats`() =
        runTest {
            val accNo = "STATS-0001"
            val submission =
                tsv {
                    line("Submission", accNo)
                    line()
                }.toString()

            webClient.submit(submission, TSV)
            waitUntil(TEN_SECONDS) { webClient.findByAccNo(accNo).toList().isNotEmpty() }

            val stats = webClient.findByAccNo(accNo).toList()
            assertThat(stats).hasSize(3)
            assertThat(stats.first().type).isEqualTo(FILES_SIZE.value)
            assertThat(stats.second().type).isEqualTo(DIRECTORIES.value)
            assertThat(stats.third().type).isEqualTo(NON_DECLARED_FILES_DIRECTORIES.value)

            val statsByType = webClient.findByType(DIRECTORIES.value).toList()
            assertThat(statsByType).hasSize(1)
            assertThat(statsByType.first().value).isEqualTo(0)
        }

    @Test
    fun `26-2 increment stats by file`() =
        runTest {
            val accNo = "STATS-0002"
            val submission1 =
                tsv {
                    line("Submission", accNo)
                    line()
                }.toString()
            webClient.submit(submission1, TSV)

            val statsRecords =
                tsv {
                    line(accNo, "150")
                    line(accNo, "100")
                }.toString()
            val statsFile = tempFolder.createFile("stats.txt", statsRecords)

            waitUntil(TEN_SECONDS) { webClient.findByAccNo(accNo).toList().isNotEmpty() }

            val result = webClient.incrementStats(VIEWS.value, statsFile)
            val stats = webClient.findByTypeAndAccNo(VIEWS.value, accNo)
            assertThat(stats).isEqualTo(SubmissionStat(accNo, 250L, VIEWS.value))
            assertThat(result.insertedRecords).isZero()
            assertThat(result.modifiedRecords).isEqualTo(2)
        }

    @Test
    fun `26-3 stats for non existing submissions are ignored`() =
        runTest {
            val accNo = "STATS-0003"
            val nonExistingAccNo = "NON-EXISTING"
            val submission1 =
                tsv {
                    line("Submission", accNo)
                    line()
                }.toString()
            webClient.submit(submission1, TSV)

            val statsRecords =
                tsv {
                    line(accNo, "20")
                    line("NON-EXISTING", "10")
                }.toString()
            val statsFile = tempFolder.createFile("stats.txt", statsRecords)

            waitUntil(TEN_SECONDS) { webClient.findByAccNo(accNo).toList().isNotEmpty() }

            val result = webClient.incrementStats(VIEWS.value, statsFile)
            val stats = webClient.findByTypeAndAccNo(VIEWS.value, accNo)
            assertThat(result.insertedRecords).isZero()
            assertThat(result.modifiedRecords).isEqualTo(1)
            assertThat(stats).isEqualTo(SubmissionStat(accNo, 20L, VIEWS.value))

            val error = assertThrows<WebClientException> { webClient.findByTypeAndAccNo(VIEWS.value, nonExistingAccNo) }
            assertThat(error)
                .hasMessageContaining("There is no submission stat registered with AccNo NON-EXISTING and type VIEWS")
        }
}
