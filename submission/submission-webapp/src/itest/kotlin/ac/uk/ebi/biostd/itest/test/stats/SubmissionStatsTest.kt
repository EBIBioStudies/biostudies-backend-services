package ac.uk.ebi.biostd.itest.test.stats

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.nfsSubmissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.model.SubmissionStat
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.flow.toList
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
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            securityTestService.ensureUserRegistration(RegularUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "true")
    fun `26-1 files size stat calculation on submit over FIRE`() =
        runTest {
            val version1 =
                tsv {
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

            val version2 =
                tsv {
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

            val fileListContent =
                tsv {
                    line("Files", "Type")
                    line("a/statsFile3.pdf", "inner")
                    line("a", "folder")
                }.toString()

            webClient.uploadFiles(
                listOf(
                    tempFolder.createFile("statsFile2.txt", "content"),
                    tempFolder.createFile("file-list.tsv", fileListContent),
                    tempFolder.createFile("stats file 1.doc", "doc content"),
                ),
            )
            webClient.uploadFiles(listOf(tempFolder.createFile("statsFile3.pdf", "pdf content")), "a")

            assertThat(webClient.submit(version1, TSV)).isSuccessful()
            waitUntil(timeout = TEN_SECONDS) { statsDataService.findByAccNo("S-STTS1").isNotEmpty() }

            val statVersion1 = statsDataService.findByAccNo("S-STTS1")
            assertThat(statVersion1).hasSize(3)
            val fileStats = statVersion1.first()
            assertThat(fileStats.value).isEqualTo(576L)
            assertThat(fileStats.type).isEqualTo(FILES_SIZE)
            assertThat(fileStats.accNo).isEqualTo("S-STTS1")

            assertThat(webClient.submit(version2, TSV)).isSuccessful()
            waitUntil(timeout = TEN_SECONDS) { statsDataService.findByAccNo("S-STTS1").first().value != 576L }

            val stats = statsDataService.findByAccNo("S-STTS1")
            assertThat(stats).hasSize(3)
            val fileSize = stats.first()
            assertThat(fileSize.value).isEqualTo(1474L)
            assertThat(fileSize.type).isEqualTo(FILES_SIZE)
            assertThat(fileSize.accNo).isEqualTo("S-STTS1")
        }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "false")
    fun `26-2 files size stat calculation on submit over NFS`() =
        runTest {
            val version1 =
                tsv {
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

            val version2 =
                tsv {
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

            val fileListContent =
                tsv {
                    line("Files", "Type")
                    line("a/statsFile3.pdf", "inner")
                    line("a", "folder")
                }.toString()

            webClient.uploadFiles(
                listOf(
                    tempFolder.createFile("statsFile2.txt", "content"),
                    tempFolder.createFile("file-list.tsv", fileListContent),
                    tempFolder.createFile("stats file 1.doc", "doc content"),
                ),
            )
            webClient.uploadFiles(listOf(tempFolder.createFile("statsFile3.pdf", "pdf content")), "a")

            assertThat(webClient.submit(version1, TSV, SubmitParameters(storageMode = NFS))).isSuccessful()
            waitUntil(timeout = TEN_SECONDS) { statsDataService.findByAccNo("S-STTS2").isNotEmpty() }
            val statVersion1 = statsDataService.findByAccNo("S-STTS2")
            assertThat(statVersion1).hasSize(3)
            assertThat(statVersion1.first().value).isEqualTo(574L)
            assertThat(statVersion1.first().type).isEqualTo(FILES_SIZE)
            assertThat(statVersion1.first().accNo).isEqualTo("S-STTS2")

            assertThat(webClient.submit(version2, TSV, SubmitParameters(storageMode = NFS))).isSuccessful()
            waitUntil(TEN_SECONDS) { statsDataService.findByAccNo("S-STTS2").first().value != 574L }

            val sub = submissionRepository.getCoreInfoByAccNoAndVersion("S-STTS2", 2)
            val subPath = nfsSubmissionPath.resolve(sub.relPath)

            val subJson = subPath.resolve("S-STTS2.json").size()
            val subTsv = subPath.resolve("S-STTS2.tsv").size()
            val fileListJson = subPath.resolve("Files/file-list.json").size()
            val fileListTsv = subPath.resolve("Files/file-list.tsv").size()
            val file1Size = subPath.resolve("Files/statsFile2.txt").size()
            val file2Size = subPath.resolve("Files/stats file 1.doc").size()
            val fileListFile = subPath.resolve("Files/a/statsFile3.pdf").size()

            val expectedSize =
                subJson + subTsv + fileListJson + fileListTsv + file1Size + file2Size + fileListFile

            val stats = statsDataService.findByAccNo("S-STTS2")
            assertThat(stats).hasSize(3)

            val fileStats = stats.first()
            assertThat(fileStats.value).isEqualTo(expectedSize)
            assertThat(fileStats.type).isEqualTo(FILES_SIZE)
            assertThat(fileStats.accNo).isEqualTo("S-STTS2")
        }

    @Test
    fun `26-3 find stats by accNo`() =
        runTest {
            val accNo = "STATS-0001"
            val submission =
                tsv {
                    line("Submission", accNo)
                }.toString()

            webClient.submit(submission, TSV)

            val stat =
                SubmissionStat(
                    accNo = accNo,
                    value = 10L,
                    type = "VIEWS",
                )
            webClient.register(stat)

            val stats = webClient.findByAccNo(accNo).toList()
            assertThat(stats).contains(stat)
        }

    @Test
    fun `26-4 find stats by type`() =
        runTest {
            val accNo = "STATS-0002"
            val submission =
                tsv {
                    line("Submission", accNo)
                }.toString()

            webClient.submit(submission, TSV)

            val vStat =
                SubmissionStat(
                    accNo = accNo,
                    value = 10L,
                    type = "VIEWS",
                )
            webClient.register(vStat)

            val stats = webClient.findByType("VIEWS").toList()
            assertThat(stats).contains(vStat)
        }

    @Test
    fun `26-5 find stats by type and AccNo`() =
        runTest {
            val accNo1 = "STATS-0003"
            val submission1 =
                tsv {
                    line("Submission", accNo1)
                }.toString()
            webClient.submit(submission1, TSV)

            val accNo2 = "STATS-0004"
            val submission2 =
                tsv {
                    line("Submission", accNo2)
                }.toString()
            webClient.submit(submission2, TSV)

            val vStat =
                SubmissionStat(
                    accNo = accNo1,
                    value = 10L,
                    type = "VIEWS",
                )
            webClient.register(vStat)

            val dStat =
                SubmissionStat(
                    accNo = accNo2,
                    value = 10L,
                    type = "VIEWS",
                )
            webClient.register(dStat)

            val stat = webClient.findByTypeAndAccNo("VIEWS", accNo1)
            assertThat(stat).isEqualTo(vStat)
        }

    @Test
    fun `26-6 register stats by file`() =
        runTest {
            val accNo = "STATS-0005"
            val submission1 =
                tsv {
                    line("Submission", accNo)
                }.toString()
            webClient.submit(submission1, TSV)

            val statsRecords =
                tsv {
                    line("STATS-0005", "100")
                    line("STATS-0010", "160")
                    line("STATS-0011")
                    line("STATS-0005", "150")
                }.toString()
            val statsFile = tempFolder.createFile("stats.txt", statsRecords)
            val result = webClient.register("VIEWS", statsFile)

            assertThat(result.insertedRecords).isOne()
            assertThat(result.modifiedRecords).isOne()

            val stats = webClient.findByTypeAndAccNo("VIEWS", accNo)
            assertThat(stats).isEqualTo(SubmissionStat(accNo, 150L, "VIEWS"))

            val notExisting = webClient.findByTypeAndAccNo("VIEWS", "STATS-0010")
            assertThat(notExisting).isEqualTo(SubmissionStat("STATS-0010", 160L, "VIEWS"))

            val error = assertThrows<WebClientException> { webClient.findByTypeAndAccNo("VIEWS", "STATS-0011") }
            assertThat(error.message)
                .contains("There is no submission stat registered with AccNo STATS-0011 and type VIEWS")
        }

    @Test
    fun `26-7 increment stats by file`() =
        runTest {
            val accNo = "STATS-0006"
            val submission1 =
                tsv {
                    line("Submission", accNo)
                }.toString()
            webClient.submit(submission1, TSV)

            val dStat =
                SubmissionStat(
                    accNo = accNo,
                    value = 10L,
                    type = "VIEWS",
                )
            webClient.register(dStat)

            val statsRecords =
                tsv {
                    line(accNo, "150")
                    line(accNo, "100")
                }.toString()
            val statsFile = tempFolder.createFile("stats.txt", statsRecords)

            val result = webClient.incrementStats("VIEWS", statsFile)
            val stats = webClient.findByTypeAndAccNo("VIEWS", accNo)
            assertThat(stats).isEqualTo(SubmissionStat(accNo, 260L, "VIEWS"))
            assertThat(result.insertedRecords).isZero()
            assertThat(result.modifiedRecords).isEqualTo(2)
        }

    @Test
    fun `26-8 refresh submission stats`() =
        runTest {
            val accNo = "STATS-WITH-DIR-0001"
            val submission =
                tsv {
                    line("Submission", accNo)
                    line()
                    line("Study")
                    line()
                    line("Files")
                    line("a-Dir")
                    line("b-Dir")
                    line("b-Dir/a_file.txt")
                }.toString()

            val subFile = tempFolder.createFile("a_file.txt", "file content")

            webClient.createFolder("a-Dir")
            webClient.uploadFile(subFile, "b-Dir")
            webClient.submit(submission, TSV)

            val stored = submissionRepository.getExtByAccNo(accNo)
            val tabFileSize = stored.pageTabFiles.map { it.size }.sum()

            val stats = webClient.refreshStats(accNo).toList()
            assertThat(stats).hasSize(3)

            val stat1 = stats.first()
            assertThat(stat1.value).isEqualTo(subFile.size() + tabFileSize)
            assertThat(stat1.type).isEqualTo("FILES_SIZE")

            val stat2 = stats[1]
            assertThat(stat2.type).isEqualTo("DIRECTORIES")
            assertThat(stat2.value).isEqualTo(2)

            val stat3 = stats[2]
            assertThat(stat3.type).isEqualTo("NON_DECLARED_FILES_DIRECTORIES")
            assertThat(stat3.value).isEqualTo(1)
        }
}
