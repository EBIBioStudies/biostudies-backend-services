package ac.uk.ebi.biostd.itest.test.stats

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.FilePersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.model.SubmissionStat
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.io.path.createTempFile

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StatsTest(
    @Autowired val securityTestService: SecurityTestService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(SuperUser)
        webClient = getWebClient(serverPort, SuperUser)
    }

    @Test
    fun `find by accNo`() {
        val accNo = "STATS-0001"
        val submission = tsv {
            line("Submission", accNo)
        }.toString()

        webClient.submitSingle(submission, SubmissionFormat.TSV)

        val stat = SubmissionStat(
            accNo = accNo,
            value = 10L,
            type = "VIEWS"
        )
        webClient.registerStat(stat)

        val stats = webClient.getStatsByAccNo(accNo)
        assertThat(stats).containsExactly(stat)
    }

    @Test
    fun `find by type`() {
        val accNo = "STATS-0002"
        val submission = tsv {
            line("Submission", accNo)
        }.toString()

        webClient.submitSingle(submission, SubmissionFormat.TSV)

        val vStat = SubmissionStat(
            accNo = accNo,
            value = 10L,
            type = "VIEWS"
        )
        webClient.registerStat(vStat)

        val dStat = SubmissionStat(
            accNo = accNo,
            value = 10L,
            type = "FILES_SIZE"
        )
        webClient.registerStat(dStat)

        val stats = webClient.getStatsByType("FILES_SIZE")
        assertThat(stats).containsExactly(dStat)
    }

    @Test
    fun `find by type and AccNo`() {
        val accNo1 = "STATS-0003"
        val submission1 = tsv {
            line("Submission", accNo1)
        }.toString()
        webClient.submitSingle(submission1, SubmissionFormat.TSV)

        val accNo2 = "STATS-0004"
        val submission2 = tsv {
            line("Submission", accNo2)
        }.toString()
        webClient.submitSingle(submission2, SubmissionFormat.TSV)

        val vStat = SubmissionStat(
            accNo = accNo1,
            value = 10L,
            type = "VIEWS"
        )
        webClient.registerStat(vStat)

        val dStat = SubmissionStat(
            accNo = accNo2,
            value = 10L,
            type = "VIEWS"
        )
        webClient.registerStat(dStat)

        val stat = webClient.getStatsByTypeAndAccNo("VIEWS", accNo1)
        assertThat(stat).isEqualTo(vStat)
    }

    @Test
    fun `register by file`() {
        val accNo = "STATS-0005"
        val submission1 = tsv {
            line("Submission", accNo)
        }.toString()
        webClient.submitSingle(submission1, SubmissionFormat.TSV)

        val statsFile = createTempFile().toFile()
        statsFile.writeText("STATS-0005\t150")

        webClient.registerStats("VIEWS", statsFile)
        val stats = webClient.getStatsByTypeAndAccNo("VIEWS", accNo)
        assertThat(stats).isEqualTo(SubmissionStat(accNo, 150L, "VIEWS"))
    }

    @Test
    fun `increment by file`() {
        val accNo = "STATS-0006"
        val submission1 = tsv {
            line("Submission", accNo)
        }.toString()
        webClient.submitSingle(submission1, SubmissionFormat.TSV)

        val dStat = SubmissionStat(
            accNo = accNo,
            value = 10L,
            type = "VIEWS"
        )
        webClient.registerStat(dStat)

        val statsFile = createTempFile().toFile()
        statsFile.writeText("$accNo\t150")

        webClient.incrementStats("VIEWS", statsFile)
        val stats = webClient.getStatsByTypeAndAccNo("VIEWS", accNo)
        assertThat(stats).isEqualTo(SubmissionStat(accNo, 160L, "VIEWS"))
    }
}
