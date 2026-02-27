package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
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
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionFilesDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ac.uk.ebi.biostd.submission.model.DoiRequest.Companion.BS_DOI_ID
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.asserts.assertThrows
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
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.flow.toList
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
import java.time.OffsetDateTime

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionPostProcessingTest(
    @param:Autowired val statsDataService: StatsDataService,
    @param:Autowired val securityTestService: SecurityTestService,
    @param:Autowired val statsDataRepository: SubmissionStatsDataRepository,
    @param:Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @param:Autowired val submissionFilesDocDataRepository: SubmissionFilesDocDataRepository,
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
                    line("DOI")
                    line()

                    line("Study")
                    line("Type", "Experiment")
                    line("File List", "file-list.tsv")
                    line()

                    line("File", "stats file 1.doc")
                    line("Type", "test")
                    line()

                    line("Author")
                    line("Name", "Jane Doe")
                    line("ORCID", "1234-5678-9101-1121")
                    line("Affiliation", "o1")
                    line()

                    line("Organization", "o1")
                    line("Name", "EMBL")
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
            val storageMode = if (enableFire) FIRE else NFS
            webClient.uploadFiles(listOf(subFile1, subFile2, fileList))
            webClient.uploadFiles(listOf(subFile3), "a")

            assertThat(webClient.submit(version1, TSV, SubmitParameters(storageMode = storageMode))).isSuccessful()
            waitUntil(TEN_SECONDS) { statsDataService.findStatsByAccNo("S-STTS1").isNotEmpty() }

            val subV1 = submissionRepository.getExtByAccNo("S-STTS1")
            val statsV1 = statsDataRepository.getByAccNo("S-STTS1")
            val expectedFilesSize = tabFilesSize(subV1) + subFile1.size()
            assertThat(statsV1.version).isEqualTo(1)
            assertThat(statsV1.released).isTrue()
            assertThat(statsV1.storageMode).isEqualTo(storageMode)
            assertThat(statsV1.subCreationTime).isEqualTo(subV1.creationTime.toInstant())
            assertThat(statsV1.subModificationTime).isEqualTo(subV1.modificationTime.toInstant())
            assertThat(statsV1.stats).hasSize(3)
            assertThat(statsV1.stats[DIRECTORIES.name]).isEqualTo(0)
            assertThat(statsV1.stats[FILES_SIZE.name]).isEqualTo(expectedFilesSize)
            assertThat(statsV1.stats[NON_DECLARED_FILES_DIRECTORIES.name]).isEqualTo(0)

            assertThat(webClient.submit(version2, TSV, SubmitParameters(storageMode = storageMode))).isSuccessful()
            waitUntil(TEN_SECONDS) { statsDataService.findStatsByAccNo("S-STTS1").first().value != expectedFilesSize }

            val subV2 = submissionRepository.getExtByAccNo("S-STTS1")
            val subFilesSize = subFile1.size() + subFile2.size() + subFile3.size()

            // Verify doi is registered
            assertThat(subV2.doi).isEqualTo("$BS_DOI_ID/S-STTS1")

            // Verify submission stats are calculated
            val statsV2 = statsDataRepository.getByAccNo("S-STTS1")
            assertThat(statsV2.version).isEqualTo(2)
            assertThat(statsV2.released).isTrue()
            assertThat(statsV2.storageMode).isEqualTo(storageMode)
            assertThat(statsV2.subCreationTime).isEqualTo(subV2.creationTime.toInstant())
            assertThat(statsV2.subModificationTime).isEqualTo(subV2.modificationTime.toInstant())
            assertThat(statsV2.stats).hasSize(3)
            assertThat(statsV2.stats[DIRECTORIES.name]).isEqualTo(1)
            assertThat(statsV2.stats[FILES_SIZE.name]).isEqualTo(tabFilesSize(subV2) + subFilesSize)
            assertThat(statsV2.stats[NON_DECLARED_FILES_DIRECTORIES.name]).isEqualTo(1)

            // Verify submission inner files are persisted
            val innerFilesV2 = submissionFilesDocDataRepository.findByAccNoAndVersion("S-STTS1", 2).toList()
            assertThat(innerFilesV2).hasSize(2)
            assertThat(innerFilesV2).satisfiesOnlyOnce { assertThat(it.file.filePath).isEqualTo("stats file 1.doc") }
            assertThat(innerFilesV2).satisfiesOnlyOnce { assertThat(it.file.filePath).isEqualTo("statsFile2.txt") }

            // Verify previous version files are deprecated
            assertThat(submissionFilesDocDataRepository.findByAccNoAndVersion("S-STTS1", 1).toList()).isEmpty()
            val innerFilesV1 = submissionFilesDocDataRepository.findByAccNoAndVersion("S-STTS1", -1).toList()
            assertThat(innerFilesV1).hasSize(1)
            assertThat(innerFilesV1).satisfiesOnlyOnce { assertThat(it.file.filePath).isEqualTo("stats file 1.doc") }

            // Verify fallback page tab files are generated
            val pageTabPath = submissionPath.resolve(subV2.relPath)
            val pageTabFallbackPath = pageTabFallbackPath.resolve(subV2.relPath)
            val jsonPageTab = pageTabFallbackPath.resolve("S-STTS1.json")
            val tsvPageTab = pageTabFallbackPath.resolve("S-STTS1.tsv")
            val jsonFileList = pageTabFallbackPath.resolve(FILES_PATH).resolve("file-list.json")
            val tsvFileList = pageTabFallbackPath.resolve(FILES_PATH).resolve("file-list.tsv")

            waitForCompletion(TEN_SECONDS) {
                assertThat(jsonPageTab).hasSameTextualContentAs(pageTabPath.resolve("S-STTS1.json"))
                assertThat(tsvPageTab).hasSameTextualContentAs(pageTabPath.resolve("S-STTS1.tsv"))

                /*
                 * TODO: this assertion is required due to a known bug causing inconsistencies on page tab generation
                 * for FIRE submissions with directories. @see https://embl.atlassian.net/browse/BIOSTD-300
                 */
                if (enableFire) {
                    assertThat(tsvFileList).hasContent(
                        """
                        Files	Type
                        a/statsFile3.pdf	inner
                        a.zip	folder
                        """.trimIndent(),
                    )
                    assertThat(jsonFileList).hasContent(
                        """
                        [{"path":"a/statsFile3.pdf","size":11,"attributes":[{"name":"Type","value":"inner"}],"type":"file"},{"path":"a.zip","size":173,"attributes":[{"name":"Type","value":"folder"}],"type":"directory"}]
                        """.trimIndent(),
                    )
                } else {
                    assertThat(tsvFileList).hasContent(
                        """
                        Files	Type
                        a/statsFile3.pdf	inner
                        a	folder
                        """.trimIndent(),
                    )
                    assertThat(jsonFileList).hasContent(
                        """
                        [{"path":"a/statsFile3.pdf","size":11,"attributes":[{"name":"Type","value":"inner"}],"type":"file"},{"path":"a","size":11,"attributes":[{"name":"Type","value":"folder"}],"type":"directory"}]
                        """.trimIndent(),
                    )
                }
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
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                }.toString()
            val submission2 =
                tsv {
                    line("Submission", accNo2)
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                }.toString()

            webClient.submit(submission1, TSV)
            webClient.submit(submission2, TSV)

            val original1 = statsDataService.findByAccNo(accNo)
            val original2 = statsDataService.findByAccNo(accNo2)

            statsDataService.deleteStatsByAccNo(accNo)
            statsDataService.deleteStatsByAccNo(accNo2)

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
                    line("File List", "file-list.tsv")
                    line()

                    line("Files")
                    line("a-Dir/a_file.txt")
                }.toString()

            val fileListContent =
                tsv {
                    line("Files", "Type")
                    line("a/statsFile3.pdf", "inner")
                }.toString()

            val params = if (enableFire) SubmitParameters(storageMode = FIRE) else SubmitParameters(storageMode = NFS)
            webClient.uploadFile(tempFolder.createFile("file-list.tsv", fileListContent))
            webClient.uploadFile(tempFolder.createFile("a_file.txt", "file content"), "a-Dir")
            webClient.uploadFile(tempFolder.createFile("statsFile3.pdf", "pdf content"), "a")
            webClient.submit(submission, TSV, params)

            val sub = submissionRepository.getExtByAccNo(accNo)
            val pageTabPath = submissionPath.resolve(sub.relPath)
            val subFiles = pageTabPath.resolve(FILES_PATH)
            val pageTabFallbackPath = pageTabFallbackPath.resolve(sub.relPath)
            val tsvPageTabFallback = pageTabFallbackPath.resolve("$accNo.tsv")
            val jsonPageTabFallback = pageTabFallbackPath.resolve("$accNo.json")
            val tsvFileListTab = pageTabFallbackPath.resolve(FILES_PATH).resolve("file-list.tsv")
            val jsonFileListTab = pageTabFallbackPath.resolve(FILES_PATH).resolve("file-list.json")

            waitForCompletion(TEN_SECONDS) { assertThat(jsonPageTabFallback).exists() }
            tsvPageTabFallback.delete()
            jsonPageTabFallback.delete()
            tsvFileListTab.delete()
            jsonFileListTab.delete()
            assertThat(tsvPageTabFallback).doesNotExist()
            assertThat(jsonPageTabFallback).doesNotExist()
            assertThat(tsvFileListTab).doesNotExist()
            assertThat(jsonFileListTab).doesNotExist()

            webClient.copyPageTab(accNo)
            waitForCompletion(TEN_SECONDS) { assertThat(jsonPageTabFallback).exists() }
            assertThat(tsvPageTabFallback).hasSameTextualContentAs(pageTabPath.resolve("$accNo.tsv"))
            assertThat(jsonPageTabFallback).hasSameTextualContentAs(pageTabPath.resolve("$accNo.json"))
            assertThat(tsvFileListTab).hasSameTextualContentAs(subFiles.resolve("file-list.tsv"))
            assertThat(jsonFileListTab).hasSameTextualContentAs(subFiles.resolve("file-list.json"))
        }

    @Test
    fun `31-4 refresh inner submission files`() =
        runTest {
            val accNo = "INNER-FILES-0001"
            val submission =
                tsv {
                    line("Submission", accNo)
                    line("ReleaseDate", "2099-09-21")
                    line()

                    line("Study")
                    line()

                    line("Files")
                    line("a-Dir/inner_file.txt")
                }.toString()

            val params = if (enableFire) SubmitParameters(storageMode = FIRE) else SubmitParameters(storageMode = NFS)
            webClient.uploadFile(tempFolder.createFile("inner_file.txt", "file content"), "a-Dir")
            webClient.submit(submission, TSV, params)

            waitForCompletion(TEN_SECONDS) {
                submissionFilesDocDataRepository.findByAccNoAndVersion(accNo, 1).toList().isNotEmpty()
            }

            submissionFilesDocDataRepository.deleteAll()
            webClient.indexInnerFiles(accNo)

            val innerFiles = submissionFilesDocDataRepository.findByAccNoAndVersion(accNo, 1).toList()
            assertThat(innerFiles).hasSize(1)
            assertThat(innerFiles).satisfiesOnlyOnce { assertThat(it.file.filePath).isEqualTo("a-Dir/inner_file.txt") }
        }

    @Test
    fun `31-5 generate doi`() =
        runTest {
            val accNo = "DOI-GEN-0001"
            val submission =
                tsv {
                    line("Submission", accNo)
                    line("Title", "DOI Generation")
                    line("ReleaseDate", "2099-09-21")
                    line()

                    line("Study")
                    line()

                    line("Author")
                    line("Name", "Jane Doe")
                    line("ORCID", "1234-5678-9101-1121")
                    line("Affiliation", "o1")
                    line()

                    line("Organization", "o1")
                    line("Name", "EMBL")
                    line()
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getExtByAccNo(accNo).doi).isNull()

            webClient.generateDoi(accNo)

            waitForCompletion(TEN_SECONDS) {
                submissionRepository.getExtByAccNo(accNo).doi == "$BS_DOI_ID/$accNo"
            }
        }

    @Test
    fun `31-6 generate already existing doi`() =
        runTest {
            val accNo = "DOI-GEN-0002"
            val submission =
                tsv {
                    line("Submission", accNo)
                    line("Title", "DOI Generation")
                    line("ReleaseDate", "2099-09-21")
                    line("DOI")
                    line()

                    line("Study")
                    line()

                    line("Author")
                    line("Name", "Jane Doe")
                    line("ORCID", "1234-5678-9101-1121")
                    line("Affiliation", "o1")
                    line()

                    line("Organization", "o1")
                    line("Name", "EMBL")
                    line()
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()
            waitForCompletion(TEN_SECONDS) {
                submissionRepository.getExtByAccNo(accNo).doi == "$BS_DOI_ID/$accNo"
            }

            val error = assertThrows<WebClientException> { webClient.generateDoi(accNo) }
            assertThat(error.message).contains("DOI already exists for submission '$accNo'")
        }
}
