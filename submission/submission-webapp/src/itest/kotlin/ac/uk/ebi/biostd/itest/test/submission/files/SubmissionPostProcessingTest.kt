package ac.uk.ebi.biostd.itest.test.submission.files

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.pageTabBackupSubmissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.privateNfsSubmissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.coroutines.waitForCompletion
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.size
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
    @EnabledIfSystemProperty(named = "enableFire", matches = "true")
    fun `31-1 submission post processing on FIRE`() =
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
            waitUntil(timeout = TEN_SECONDS) { statsDataService.findStatsByAccNo("S-STTS1").isNotEmpty() }

            val statVersion1 = statsDataService.findStatsByAccNo("S-STTS1")
            assertThat(statVersion1).hasSize(3)
            val fileStats = statVersion1.first()
            assertThat(fileStats.value).isEqualTo(576L)
            assertThat(fileStats.type).isEqualTo(FILES_SIZE)
            assertThat(fileStats.accNo).isEqualTo("S-STTS1")

            assertThat(webClient.submit(version2, TSV)).isSuccessful()
            waitUntil(timeout = TEN_SECONDS) { statsDataService.findStatsByAccNo("S-STTS1").first().value != 576L }

            // Verify submission stats are calculated
            val stats = statsDataService.findStatsByAccNo("S-STTS1")
            assertThat(stats).hasSize(3)
            val fileSize = stats.first()
            assertThat(fileSize.value).isEqualTo(1474L)
            assertThat(fileSize.type).isEqualTo(FILES_SIZE)
            assertThat(fileSize.accNo).isEqualTo("S-STTS1")

            // Verify page tab files are backed up
            val sub = submissionRepository.getExtByAccNo("S-STTS1")
            val pageTabBackupPath = pageTabBackupSubmissionPath.resolve(sub.relPath)
            waitForCompletion(TEN_SECONDS) {
                val jsonPageTab = pageTabBackupPath.resolve("S-STTS1.json")
                assertThat(jsonPageTab).exists()

                val tsvPageTab = pageTabBackupPath.resolve("S-STTS1.tsv")
                assertThat(tsvPageTab).exists()

                val jsonFileListTab = pageTabBackupPath.resolve("Files").resolve("file-list.json")
                assertThat(jsonFileListTab).exists()

                val tsvFileListTab = pageTabBackupPath.resolve("Files").resolve("file-list.tsv")
                assertThat(tsvFileListTab).exists()
            }
        }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "false")
    fun `31-2 submission post processing on NFS`() =
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
            waitUntil(timeout = TEN_SECONDS) { statsDataService.findStatsByAccNo("S-STTS2").isNotEmpty() }
            val statVersion1 = statsDataService.findStatsByAccNo("S-STTS2")
            assertThat(statVersion1).hasSize(3)
            assertThat(statVersion1.first().value).isEqualTo(574L)
            assertThat(statVersion1.first().type).isEqualTo(FILES_SIZE)
            assertThat(statVersion1.first().accNo).isEqualTo("S-STTS2")

            assertThat(webClient.submit(version2, TSV, SubmitParameters(storageMode = NFS))).isSuccessful()
            waitUntil(TEN_SECONDS) { statsDataService.findStatsByAccNo("S-STTS2").first().value != 574L }

            val sub = submissionRepository.getCoreInfoByAccNoAndVersion("S-STTS2", 2)
            val subPath = privateNfsSubmissionPath.resolve(sub.relPath)

            val subJson = subPath.resolve("S-STTS2.json").size()
            val subTsv = subPath.resolve("S-STTS2.tsv").size()
            val fileListJson = subPath.resolve("Files/file-list.json").size()
            val fileListTsv = subPath.resolve("Files/file-list.tsv").size()
            val file1Size = subPath.resolve("Files/statsFile2.txt").size()
            val file2Size = subPath.resolve("Files/stats file 1.doc").size()
            val fileListFile = subPath.resolve("Files/a/statsFile3.pdf").size()

            val expectedSize =
                subJson + subTsv + fileListJson + fileListTsv + file1Size + file2Size + fileListFile

            // Verify stats are calculated
            val stats = statsDataService.findStatsByAccNo("S-STTS2")
            assertThat(stats).hasSize(3)
            val fileStats = stats.first()
            assertThat(fileStats.value).isEqualTo(expectedSize)
            assertThat(fileStats.type).isEqualTo(FILES_SIZE)
            assertThat(fileStats.accNo).isEqualTo("S-STTS2")

            // Verify page tab files are backed up
            val pageTabBackupPath = pageTabBackupSubmissionPath.resolve(sub.relPath)
            waitForCompletion(TEN_SECONDS) {
                val jsonPageTab = pageTabBackupPath.resolve("S-STTS2.json")
                assertThat(jsonPageTab).exists()

                val tsvPageTab = pageTabBackupPath.resolve("S-STTS2.tsv")
                assertThat(tsvPageTab).exists()

                val jsonFileListTab = pageTabBackupPath.resolve("Files").resolve("file-list.json")
                assertThat(jsonFileListTab).exists()

                val tsvFileListTab = pageTabBackupPath.resolve("Files").resolve("file-list.tsv")
                assertThat(tsvFileListTab).exists()
            }
        }
}
