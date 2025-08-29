package ac.uk.ebi.biostd.itest.test.submission.files

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.enableFire
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.pageTabFallbackPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.DIRECTORIES
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.NON_DECLARED_FILES_DIRECTORIES
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.coroutines.waitForCompletion
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.PersistedExtFile
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.extended.model.allPageTabFiles
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Durations.TEN_SECONDS
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.nio.file.Path
import java.time.OffsetDateTime

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionPostProcessingTest(
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
    fun `31-1 submission post processing`() =
        runTest {
            fun tabFilesSize(sub: ExtSubmission) =
                sub.allPageTabFiles
                    .filterIsInstance<PersistedExtFile>()
                    .sumOf { it.size }

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

            val fileList = tempFolder.createFile("file-list.tsv", fileListContent)
            val subFile1 = tempFolder.createFile("stats file 1.doc", "doc content")
            val subFile2 = tempFolder.createFile("statsFile2.txt", "content")
            val subFile3 = tempFolder.createFile("statsFile3.pdf", "pdf content")
            val params = if (enableFire) SubmitParameters(storageMode = NFS) else SubmitParameters(storageMode = FIRE)
            webClient.uploadFiles(listOf(subFile1, subFile2, fileList))
            webClient.uploadFiles(listOf(subFile3), "a")

            assertThat(webClient.submit(version1, TSV, params)).isSuccessful()
            waitUntil(TEN_SECONDS) { statsDataService.findStatsByAccNo("S-STTS1").isNotEmpty() }

            val subV1 = submissionRepository.getExtByAccNo("S-STTS1")
            val statsV1 = statsDataService.findStatsByAccNo("S-STTS1")
            val expectedFilesSize = tabFilesSize(subV1) + subFile1.size()
            assertThat(statsV1).satisfiesOnlyOnce {
                assertThat(it.type).isEqualTo(DIRECTORIES)
                assertThat(it.value).isEqualTo(0)
            }
            assertThat(statsV1).satisfiesOnlyOnce {
                assertThat(it.type).isEqualTo(FILES_SIZE)
                assertThat(it.value).isEqualTo(expectedFilesSize)
            }
            assertThat(statsV1).satisfiesOnlyOnce {
                assertThat(it.type).isEqualTo(NON_DECLARED_FILES_DIRECTORIES)
                assertThat(it.value).isEqualTo(0)
            }

            assertThat(webClient.submit(version2, TSV, params)).isSuccessful()
            waitUntil(TEN_SECONDS) { statsDataService.findStatsByAccNo("S-STTS1").first().value != expectedFilesSize }

            val subV2 = submissionRepository.getExtByAccNo("S-STTS1")
            val subFilesSize = subFile1.size() + subFile2.size() + subFile3.size()

            // Verify submission stats are calculated
            val statsV2 = statsDataService.findStatsByAccNo("S-STTS1")
            assertThat(statsV2).hasSize(3)
            assertThat(statsV2).satisfiesOnlyOnce {
                assertThat(it.type).isEqualTo(DIRECTORIES)
                assertThat(it.value).isEqualTo(1)
            }
            assertThat(statsV2).satisfiesOnlyOnce {
                assertThat(it.type).isEqualTo(FILES_SIZE)
                assertThat(it.value).isEqualTo(tabFilesSize(subV2) + subFilesSize)
            }
            assertThat(statsV2).satisfiesOnlyOnce {
                assertThat(it.type).isEqualTo(NON_DECLARED_FILES_DIRECTORIES)
                assertThat(it.value).isEqualTo(1)
            }

            // Verify fallback page tab files are generated
            val sub = submissionRepository.getExtByAccNo("S-STTS1")
            val pageTabFallbackPath = pageTabFallbackPath.resolve(sub.relPath)
            waitForCompletion(TEN_SECONDS) {
                val jsonPageTab = pageTabFallbackPath.resolve("S-STTS1.json")
                assertThat(jsonPageTab).exists()

                val tsvPageTab = pageTabFallbackPath.resolve("S-STTS1.tsv")
                assertThat(tsvPageTab).exists()

                val jsonFileListTab = pageTabFallbackPath.resolve("Files").resolve("file-list.json")
                assertThat(jsonFileListTab).exists()

                val tsvFileListTab = pageTabFallbackPath.resolve("Files").resolve("file-list.tsv")
                assertThat(tsvFileListTab).exists()
            }
        }

    @Test
    fun `31-2 refresh submissions stats`() =
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

            webClient.recalculateStats(accNo)
            webClient.recalculateStats(accNo2)

            assertThat(statsDataService.findByAccNo(accNo)).isEqualTo(original1)
            assertThat(statsDataService.findByAccNo(accNo2)).isEqualTo(original2)
        }

    @Test
    fun `31-3 refresh pagetab fallback files`() =
        runTest {
            val accNo = "STATS-WITH-DIR-0001"
            val submission =
                tsv {
                    line("Submission", accNo)
                    line("ReleaseDate", "2099-09-21")
                    line()

                    line("Study")
                    line()

                    line("Files")
                    line("a-Dir/a_file.txt")
                }.toString()

            val storageMode = if (enableFire) FIRE else NFS
            webClient.uploadFile(tempFolder.createFile("a_file.txt", "file content"), "a-Dir")
            webClient.submit(submission, TSV, SubmitParameters(storageMode = storageMode))

            val sub = submissionRepository.getExtByAccNo(accNo)
            val pageTabPath = submissionPath.resolve(sub.relPath)
            val tsv = Path.of("$pageTabPath/$accNo.tsv").toFile()
            val json = Path.of("$pageTabPath/$accNo.json").toFile()

            val pageTabFallbackPath = pageTabFallbackPath.resolve(sub.relPath)
            val tsvPageTabFallback = pageTabFallbackPath.resolve("$accNo.tsv")
            val jsonPageTabFallback = pageTabFallbackPath.resolve("$accNo.json")

            waitForCompletion(TEN_SECONDS) { assertThat(jsonPageTabFallback).exists() }
            tsvPageTabFallback.delete()
            jsonPageTabFallback.delete()
            assertThat(tsvPageTabFallback).doesNotExist()
            assertThat(jsonPageTabFallback).doesNotExist()

            webClient.copyPageTab(accNo)
            waitForCompletion(TEN_SECONDS) { assertThat(jsonPageTabFallback).exists() }
            assertThat(tsvPageTabFallback).hasSameTextualContentAs(tsv)
            assertThat(jsonPageTabFallback).hasSameTextualContentAs(json)
        }
}
