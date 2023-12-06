package ac.uk.ebi.biostd.itest.test.stats

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.model.SubmissionStat
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Durations.TEN_SECONDS
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
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
class SubmissionStatsTest(
    @Autowired val statsDataService: StatsDataService,
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() = runBlocking {
        securityTestService.ensureUserRegistration(SuperUser)
        securityTestService.ensureUserRegistration(RegularUser)
        webClient = getWebClient(serverPort, SuperUser)
    }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "true")
    fun `26-1 files size stat calculation on submit over FIRE`() = runTest {
        val version1 = tsv {
            line("Submission", "S-STTS1")
            line("Title", "Stats Registration Test Over FIRE")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()

            line("Study")
            line()

            line("File", "stats file 1.doc")
            line("Type", "test")
            line()
        }.toString()

        val version2 = tsv {
            line("Submission", "S-STTS1")
            line("Title", "Stats Registration Test Over FIRE")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()

            line("Study")
            line("Type", "Experiment")
            line("File List", "file-list.tsv")
            line()

            line("File", "stats file 1.doc")
            line("Type", "test")
            line()

            line("Experiment", "Exp1")
            line("Type", "Subsection")
            line()

            line("File", "statsFile2.txt")
            line("Type", "Attached")
            line()
        }.toString()

        val fileListContent = tsv {
            line("Files", "Type")
            line("a/statsFile3.pdf", "inner")
            line("a", "folder")
        }.toString()

        webClient.uploadFiles(
            listOf(
                tempFolder.createFile("statsFile2.txt", "content"),
                tempFolder.createFile("file-list.tsv", fileListContent),
                tempFolder.createFile("stats file 1.doc", "doc content"),
            )
        )
        webClient.uploadFiles(listOf(tempFolder.createFile("statsFile3.pdf", "pdf content")), "a")

        assertThat(webClient.submitSingle(version1, TSV)).isSuccessful()
        waitUntil(TEN_SECONDS) { statsDataService.findByAccNo("S-STTS1").isNotEmpty() }

        val statVersion1 = statsDataService.findByAccNo("S-STTS1")
        assertThat(statVersion1).hasSize(1)
        assertThat(statVersion1.first().value).isEqualTo(1211L)
        assertThat(statVersion1.first().type).isEqualTo(FILES_SIZE)
        assertThat(statVersion1.first().accNo).isEqualTo("S-STTS1")

        assertThat(webClient.submitSingle(version2, TSV)).isSuccessful()
        waitUntil(TEN_SECONDS) { statsDataService.findByAccNo("S-STTS1").first().value != 1211L }
        val stats = statsDataService.findByAccNo("S-STTS1")
        assertThat(stats).hasSize(1)
        assertThat(stats.first().value).isEqualTo(3529L)
        assertThat(stats.first().type).isEqualTo(FILES_SIZE)
        assertThat(stats.first().accNo).isEqualTo("S-STTS1")
    }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "false")
    fun `26-2 files size stat calculation on submit over NFS`() = runTest {
        val version1 = tsv {
            line("Submission", "S-STTS2")
            line("Title", "Stats Registration Test Over NFS")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()

            line("Study")
            line()

            line("File", "stats file 1.doc")
            line("Type", "test")
            line()
        }.toString()

        val version2 = tsv {
            line("Submission", "S-STTS2")
            line("Title", "Stats Registration Test Over NFS")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()

            line("Study")
            line("Type", "Experiment")
            line("File List", "file-list.tsv")
            line()

            line("File", "stats file 1.doc")
            line("Type", "test")
            line()

            line("Experiment", "Exp1")
            line("Type", "Subsection")
            line()

            line("File", "statsFile2.txt")
            line("Type", "Attached")
            line()
        }.toString()

        val fileListContent = tsv {
            line("Files", "Type")
            line("a/statsFile3.pdf", "inner")
            line("a", "folder")
        }.toString()

        webClient.uploadFiles(
            listOf(
                tempFolder.createFile("statsFile2.txt", "content"),
                tempFolder.createFile("file-list.tsv", fileListContent),
                tempFolder.createFile("stats file 1.doc", "doc content"),
            )
        )
        webClient.uploadFiles(listOf(tempFolder.createFile("statsFile3.pdf", "pdf content")), "a")

        assertThat(webClient.submitSingle(version1, TSV, NFS)).isSuccessful()
        waitUntil(TEN_SECONDS) { statsDataService.findByAccNo("S-STTS2").isNotEmpty() }
        val statVersion1 = statsDataService.findByAccNo("S-STTS2")
        assertThat(statVersion1).hasSize(1)
        assertThat(statVersion1.first().value).isEqualTo(1208L)
        assertThat(statVersion1.first().type).isEqualTo(FILES_SIZE)
        assertThat(statVersion1.first().accNo).isEqualTo("S-STTS2")

        assertThat(webClient.submitSingle(version2, TSV, NFS)).isSuccessful()
        waitUntil(TEN_SECONDS) { statsDataService.findByAccNo("S-STTS2").first().value != 1208L }
        val stats = statsDataService.findByAccNo("S-STTS2")
        assertThat(stats).hasSize(1)
        assertThat(stats.first().value).isEqualTo(3364L)
        assertThat(stats.first().type).isEqualTo(FILES_SIZE)
        assertThat(stats.first().accNo).isEqualTo("S-STTS2")
    }

    @Test
    fun `26-3 find stats by accNo`() {
        val accNo = "STATS-0001"
        val submission = tsv {
            line("Submission", accNo)
        }.toString()

        webClient.submitSingle(submission, TSV)

        val stat = SubmissionStat(
            accNo = accNo,
            value = 10L,
            type = "VIEWS"
        )
        webClient.registerStat(stat)

        val stats = webClient.getStatsByAccNo(accNo)
        assertThat(stats).contains(stat)
    }

    @Test
    fun `26-4 find stats by type`() {
        val accNo = "STATS-0002"
        val submission = tsv {
            line("Submission", accNo)
        }.toString()

        webClient.submitSingle(submission, TSV)

        val vStat = SubmissionStat(
            accNo = accNo,
            value = 10L,
            type = "VIEWS"
        )
        webClient.registerStat(vStat)

        val stats = webClient.getStatsByType("VIEWS")
        assertThat(stats).contains(vStat)
    }

    @Test
    fun `26-5 find stats by type and AccNo`() {
        val accNo1 = "STATS-0003"
        val submission1 = tsv {
            line("Submission", accNo1)
        }.toString()
        webClient.submitSingle(submission1, TSV)

        val accNo2 = "STATS-0004"
        val submission2 = tsv {
            line("Submission", accNo2)
        }.toString()
        webClient.submitSingle(submission2, TSV)

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
    fun `26-6 register stats by file`() {
        val accNo = "STATS-0005"
        val submission1 = tsv {
            line("Submission", accNo)
        }.toString()
        webClient.submitSingle(submission1, TSV)

        val statsFile = kotlin.io.path.createTempFile().toFile()
        statsFile.writeText("STATS-0005\t150")

        webClient.registerStats("VIEWS", statsFile)
        val stats = webClient.getStatsByTypeAndAccNo("VIEWS", accNo)
        assertThat(stats).isEqualTo(SubmissionStat(accNo, 150L, "VIEWS"))
    }

    @Test
    fun `26-7 increment stats by file`() {
        val accNo = "STATS-0006"
        val submission1 = tsv {
            line("Submission", accNo)
        }.toString()
        webClient.submitSingle(submission1, TSV)

        val dStat = SubmissionStat(
            accNo = accNo,
            value = 10L,
            type = "VIEWS"
        )
        webClient.registerStat(dStat)

        val statsFile = kotlin.io.path.createTempFile().toFile()
        statsFile.writeText("$accNo\t150")

        webClient.incrementStats("VIEWS", statsFile)
        val stats = webClient.getStatsByTypeAndAccNo("VIEWS", accNo)
        assertThat(stats).isEqualTo(SubmissionStat(accNo, 160L, "VIEWS"))
    }
}
