package ac.uk.ebi.biostd.itest.test.stats

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.pageTabBackupSubmissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.model.PersistedExtFile
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.model.SubmissionStat
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
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
    fun `26-1 find stats by accNo`() =
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
    fun `26-2 find stats by type`() =
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
    fun `26-3 find stats by type and AccNo`() =
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
    fun `26-4 register stats by file`() =
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
    fun `26-5 increment stats by file`() =
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
    @EnabledIfSystemProperty(named = "enableFire", matches = "true")
    fun `26-6 refresh submission stats`() =
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
            val tabFileSize =
                stored.pageTabFiles
                    .filterIsInstance<PersistedExtFile>()
                    .map { it.size }
                    .sum()

            val stats = webClient.refreshStats(accNo).toList().sortedBy { it.type }
            assertThat(stats).hasSize(3)

            val stat2 = stats[0]
            assertThat(stat2.type).isEqualTo("DIRECTORIES")
            assertThat(stat2.value).isEqualTo(2)

            val stat1 = stats[1]
            assertThat(stat1.value).isEqualTo(subFile.size() + tabFileSize)
            assertThat(stat1.type).isEqualTo("FILES_SIZE")

            val stat3 = stats[2]
            assertThat(stat3.type).isEqualTo("NON_DECLARED_FILES_DIRECTORIES")
            assertThat(stat3.value).isEqualTo(1)

            val baseFolder = "STATS-WITH-DIR-/001/STATS-WITH-DIR-0001"
            val jsonCopyFile = pageTabBackupSubmissionPath.resolve("$baseFolder/STATS-WITH-DIR-0001.json")
            assertThat(jsonCopyFile).hasContent(
                """
                {
                  "accno" : "STATS-WITH-DIR-0001",
                  "section" : {
                    "type" : "Study",
                    "files" : [ [ {
                      "path" : "a-Dir.zip",
                      "size" : 22,
                      "type" : "directory"
                    }, {
                      "path" : "b-Dir.zip",
                      "size" : 166,
                      "type" : "directory"
                    }, {
                      "path" : "b-Dir/a_file.txt",
                      "size" : 12,
                      "type" : "file"
                    } ] ]
                  },
                  "type" : "submission"
                }
                """.trimIndent(),
            )

            val tsvCopyFile = pageTabBackupSubmissionPath.resolve("$baseFolder/STATS-WITH-DIR-0001.tsv")
            assertThat(tsvCopyFile).hasContent(
                """
                Submission	STATS-WITH-DIR-0001

                Study

                Files
                a-Dir.zip
                b-Dir.zip
                b-Dir/a_file.txt
                """.trimIndent(),
            )
        }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "false")
    fun `26-6-2 refresh submission stats NFS`() =
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
            val tabFileSize =
                stored.pageTabFiles
                    .filterIsInstance<PersistedExtFile>()
                    .map { it.size }
                    .sum()

            val stats = webClient.refreshStats(accNo).toList().sortedBy { it.type }
            assertThat(stats).hasSize(3)

            val stat2 = stats[0]
            assertThat(stat2.type).isEqualTo("DIRECTORIES")
            assertThat(stat2.value).isEqualTo(2)

            val stat1 = stats[1]
            assertThat(stat1.value).isEqualTo(subFile.size() + tabFileSize)
            assertThat(stat1.type).isEqualTo("FILES_SIZE")

            val stat3 = stats[2]
            assertThat(stat3.type).isEqualTo("NON_DECLARED_FILES_DIRECTORIES")
            assertThat(stat3.value).isEqualTo(1)

            val baseFolder = "STATS-WITH-DIR-/001/STATS-WITH-DIR-0001"
            val jsonCopyFile = pageTabBackupSubmissionPath.resolve("$baseFolder/STATS-WITH-DIR-0001.json")
            assertThat(jsonCopyFile).hasContent(
                """
                {
                  "accno" : "STATS-WITH-DIR-0001",
                  "section" : {
                    "type" : "Study",
                    "files" : [ [ {
                      "path" : "a-Dir",
                      "size" : 0,
                      "type" : "directory"
                    }, {
                      "path" : "b-Dir",
                      "size" : 12,
                      "type" : "directory"
                    }, {
                      "path" : "b-Dir/a_file.txt",
                      "size" : 12,
                      "type" : "file"
                    } ] ]
                  },
                  "type" : "submission"
                }
                """.trimIndent(),
            )

            val tsvCopyFile = pageTabBackupSubmissionPath.resolve("$baseFolder/STATS-WITH-DIR-0001.tsv")
            assertThat(tsvCopyFile).hasContent(
                """
                Submission	STATS-WITH-DIR-0001

                Study

                Files
                a-Dir
                b-Dir
                b-Dir/a_file.txt
                """.trimIndent(),
            )
        }

    @Test
    fun `26-7 refresh all submissions stats`() =
        runTest {
            val accNo = "STATS-2691"
            val accNo2 = "STATS-2692"

            val submission1 =
                tsv {
                    line("Submission", accNo)
                }.toString()
            val submission2 =
                tsv {
                    line("Submission", accNo2)
                }.toString()

            webClient.submit(submission1, TSV)
            webClient.submit(submission2, TSV)

            val original1 = statsDataService.findByAccNo(accNo)
            val original2 = statsDataService.findByAccNo(accNo2)

            statsDataService.cleanStatsByAccNo(accNo)
            statsDataService.cleanStatsByAccNo(accNo2)

            webClient.refreshAllStats()

            assertThat(statsDataService.findByAccNo(accNo)).isEqualTo(original1)
            assertThat(statsDataService.findByAccNo(accNo2)).isEqualTo(original2)
        }

    @Test
    fun `26-8 private submission with double blind review stats are not filter`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "STATSDB-0001")
                    line("ReleaseDate", "2099-09-21")
                    line("ReviewType", "DoubleBlind")
                    line()

                    line("Study", "SECT-001")
                    line("Type", "Experiment")
                    line()

                    line("Author", "a1")
                    line("Name", "Jane Doe")
                    line()

                    line("Organization", "o1")
                    line("Name", "EMBL")
                    line()
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val baseFolder = "STATSDB-/001/STATSDB-0001"
            val jsonCopyFile = pageTabBackupSubmissionPath.resolve("$baseFolder/STATSDB-0001.json")
            assertThat(jsonCopyFile).hasContent(
                """
                {
                  "accno" : "STATSDB-0001",
                  "attributes" : [ {
                    "name" : "ReviewType",
                    "value" : "DoubleBlind"
                  }, {
                    "name" : "ReleaseDate",
                    "value" : "2099-09-21"
                  } ],
                  "section" : {
                    "accno" : "SECT-001",
                    "type" : "Study",
                    "attributes" : [ {
                      "name" : "Type",
                      "value" : "Experiment"
                    } ],
                    "subsections" : [ {
                      "accno" : "a1",
                      "type" : "Author",
                      "attributes" : [ {
                        "name" : "Name",
                        "value" : "Jane Doe"
                      } ]
                    }, {
                      "accno" : "o1",
                      "type" : "Organization",
                      "attributes" : [ {
                        "name" : "Name",
                        "value" : "EMBL"
                      } ]
                    } ]
                  },
                  "type" : "submission"
                }
                """.trimIndent(),
            )

            val tsvCopyFile = pageTabBackupSubmissionPath.resolve("$baseFolder/STATSDB-0001.tsv")
            assertThat(tsvCopyFile).hasContent(
                """
                Submission	STATSDB-0001
                ReviewType	DoubleBlind
                ReleaseDate	2099-09-21

                Study	SECT-001
                Type	Experiment

                Author	a1	SECT-001
                Name	Jane Doe

                Organization	o1	SECT-001
                Name	EMBL
                """.trimIndent(),
            )
        }
}
