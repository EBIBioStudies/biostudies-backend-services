package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.ftpPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.AccessType.UPDATE_PUBLIC
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.createOrReplaceFile
import ebi.ac.uk.model.RequestStatus.INVALID
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.awaitility.Durations.TWO_SECONDS
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.time.OffsetDateTime

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ResubmissionApiTest(
    @Autowired val mongoTemplate: ReactiveMongoTemplate,
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val requestRepository: SubmissionRequestPersistenceService,
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

    @Nested
    inner class Resubmit {
        @Test
        fun `5-1 resubmit private existing submission`() =
            runTest {
                val submission =
                    tsv {
                        line("Submission", "S-RSTST1")
                        line("Title", "Simple Submission With Files")
                        line("ReleaseDate", "2124-07-16")
                        line()

                        line("Study")
                        line("Type", "Experiment")
                        line("File List", "file-list.tsv")
                        line()

                        line("File", "file section.doc")
                        line("Type", "test")
                        line()

                        line("Experiment", "Exp1")
                        line("Type", "Subsection")
                        line()

                        line("File", "fileSubSection.txt")
                        line("Type", "Attached")
                        line()
                    }.toString()

                val fileListContent =
                    tsv {
                        line("Files", "Type")
                        line("a/fileFileList.pdf", "inner")
                        line("a", "folder")
                    }.toString()

                webClient.uploadFiles(
                    listOf(
                        tempFolder.createFile("fileSubSection.txt", "content"),
                        tempFolder.createFile("file-list.tsv", fileListContent),
                        tempFolder.createFile("file section.doc", "doc content"),
                    ),
                )
                webClient.uploadFiles(listOf(tempFolder.createFile("fileFileList.pdf", "pdf content")), "a")
                assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

                val sub = submissionRepository.getExtByAccNo("S-RSTST1")
                assertThat(sub.version).isEqualTo(1)
                assertThat(File("$submissionPath/${sub.relPath}/Files/file section.doc")).hasContent("doc content")
                assertThat(File("$submissionPath/${sub.relPath}/Files/fileSubSection.txt")).hasContent("content")
                assertThat(File("$submissionPath/${sub.relPath}/Files/a/fileFileList.pdf")).hasContent("pdf content")

                val changedFile = tempFolder.resolve("fileSubSection.txt").apply { writeText("newContent") }
                webClient.uploadFiles(listOf(changedFile))
                assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

                val subV2 = submissionRepository.getExtByAccNo("S-RSTST1")
                assertThat(subV2.version).isEqualTo(2)
                assertThat(File("$submissionPath/${subV2.relPath}/Files/file section.doc")).exists()
                assertThat(File("$submissionPath/${subV2.relPath}/Files/fileSubSection.txt")).hasContent("newContent")
                assertThat(File("$submissionPath/${subV2.relPath}/Files/a/fileFileList.pdf")).exists()
            }

        @Test
        fun `5-2 resubmit existing submission with the same files`() =
            runTest {
                val submission =
                    tsv {
                        line("Submission", "S-RSTST2")
                        line("Title", "Simple Submission With Files 2")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()

                        line("Study")
                        line("Type", "Experiment")
                        line("File List", "file-list.tsv")
                        line()

                        line("File", "file section.doc")
                        line("Type", "test")
                        line()

                        line("Experiment", "Exp1")
                        line("Type", "Subsection")
                        line()

                        line("File", "fileSubSection.txt")
                        line("Type", "Attached")
                        line()
                    }.toString()

                val fileListContent =
                    tsv {
                        line("Files", "Type")
                        line("a/fileFileList.pdf", "inner")
                        line("a", "folder")
                    }.toString()

                webClient.uploadFiles(
                    listOf(
                        tempFolder.createFile("fileSubSection.txt", "content"),
                        tempFolder.createFile("file-list.tsv", fileListContent),
                        tempFolder.createFile("file section.doc"),
                    ),
                )
                webClient.uploadFiles(listOf(tempFolder.createFile("fileFileList.pdf")), "a")
                assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

                val sub = submissionRepository.getExtByAccNo("S-RSTST2")
                assertThat(sub.version).isEqualTo(1)
                assertThat(File("$submissionPath/${sub.relPath}/Files/file section.doc")).exists()
                assertThat(File("$submissionPath/${sub.relPath}/Files/fileSubSection.txt")).exists()
                assertThat(File("$submissionPath/${sub.relPath}/Files/fileSubSection.txt")).hasContent("content")
                assertThat(File("$submissionPath/${sub.relPath}/Files/a/fileFileList.pdf")).exists()

                assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
                val subV2 = submissionRepository.getExtByAccNo("S-RSTST2")
                assertThat(subV2.version).isEqualTo(2)
                assertThat(File("$submissionPath/${subV2.relPath}/Files/file section.doc")).exists()
                assertThat(File("$submissionPath/${subV2.relPath}/Files/fileSubSection.txt")).exists()
                assertThat(File("$submissionPath/${subV2.relPath}/Files/fileSubSection.txt")).hasContent("content")
                assertThat(File("$submissionPath/${subV2.relPath}/Files/a/fileFileList.pdf")).exists()
            }

        @Test
        fun `5-3 re submit a submission with rootPath`() {
            val rootPath = "The-RootPath"
            val dataFile = "DataFile1.txt"

            val submission =
                tsv {
                    line("Submission", "S-RSTST3")
                    line("Title", "Sample Submission")
                    line("RootPath", rootPath)
                    line()
                    line("Study")
                    line()
                    line("File", "DataFile1.txt")
                    line()
                }.toString()

            webClient.uploadFiles(listOf(tempFolder.createFile("DataFile1.txt")), rootPath)
            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

            webClient.deleteFile(dataFile, rootPath)

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
        }
    }

    @Nested
    inner class Suppress {
        @Test
        fun `5-4 regular user suppresses own submission`() {
            val version1 =
                tsv {
                    line("Submission", "S-RSTST4")
                    line("Title", "Public Submission")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Study")
                    line()
                }.toString()

            val regularUser = RegularUser.email
            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, regularUser)

            assertThat(onBehalfClient.submitSingle(version1, TSV)).isSuccessful()

            val version2 =
                tsv {
                    line("Submission", "S-RSTST4")
                    line("Title", "Suppressed Submission")
                    line("ReleaseDate", "2050-05-22")
                    line()
                    line("Study")
                    line()
                }.toString()

            assertThatExceptionOfType(WebClientException::class.java)
                .isThrownBy { getWebClient(serverPort, RegularUser).submitSingle(version2, TSV) }
                .withMessageContaining("The release date of a public study cannot be changed")
        }

        @Test
        fun `5-5 super user suppresses submission from another user`() =
            runTest {
                val version1 =
                    tsv {
                        line("Submission", "S-RSTST5")
                        line("Title", "Public Submission")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line()
                        line("File", "file_5-5-1.txt")
                        line()
                        line("File", "file_5-5-2.txt")
                        line()
                    }.toString()

                val onBehalfClient =
                    SecurityWebClient
                        .create("http://localhost:$serverPort")
                        .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

                webClient.uploadFile(tempFolder.createFile("file_5-5-1.txt", "5-5-1 content"))
                webClient.uploadFile(tempFolder.createFile("file_5-5-2.txt", "5-5-2 content"))
                assertThat(onBehalfClient.submitSingle(version1, TSV)).isSuccessful()

                val subV1 = submissionRepository.getExtByAccNo("S-RSTST5")
                val ftpFilesV1 = FileUtils.listAllFiles(File("$ftpPath/${subV1.relPath}/Files"))
                val ftpFile1 = File("$ftpPath/${subV1.relPath}/Files/file_5-5-1.txt")
                val ftpFile2 = File("$ftpPath/${subV1.relPath}/Files/file_5-5-2.txt")

                assertThat(ftpFilesV1).containsOnly(ftpFile1, ftpFile2)
                assertThat(ftpFile1).hasContent("5-5-1 content")
                assertThat(ftpFile2).hasContent("5-5-2 content")
                assertThat(File("$submissionPath/${subV1.relPath}/Files/file_5-5-1.txt")).hasContent("5-5-1 content")
                assertThat(File("$submissionPath/${subV1.relPath}/Files/file_5-5-2.txt")).hasContent("5-5-2 content")

                val version2 =
                    tsv {
                        line("Submission", "S-RSTST5")
                        line("Title", "Suppressed Submission")
                        line("ReleaseDate", "2050-05-22")
                        line()
                        line("Study")
                        line()
                        line("File", "file_5-5-1.txt")
                        line()
                        line("File", "file_5-5-2.txt")
                        line()
                    }.toString()

                assertThat(onBehalfClient.submitSingle(version2, TSV)).isSuccessful()

                val subV2 = submissionRepository.getExtByAccNo("S-RSTST5")

                assertThat(File("$ftpPath/${subV2.relPath}/Files")).isEmptyDirectory()
                assertThat(File("$ftpPath/${subV2.relPath}/Files/file_5-5-1.txt")).doesNotExist()
                assertThat(File("$ftpPath/${subV2.relPath}/Files/file_5-5-2.txt")).doesNotExist()
                val submissionFilesV2 = FileUtils.listAllFiles(File("$submissionPath/${subV2.relPath}/Files"))
                val expectedFile1 = File("$submissionPath/${subV2.relPath}/Files/file_5-5-1.txt")
                val expectedFile2 = File("$submissionPath/${subV2.relPath}/Files/file_5-5-2.txt")
                assertThat(submissionFilesV2).containsOnly(expectedFile1, expectedFile2)
                assertThat(expectedFile1).hasContent("5-5-1 content")
                assertThat(expectedFile2).hasContent("5-5-2 content")
            }
    }

    @Nested
    inner class ModifyPublicMetadata {
        @Test
        fun `5-6 modify metadata of a public submission`() =
            runTest {
                val version1 =
                    tsv {
                        line("Submission", "S-RSTST6")
                        line("Title", "Public Submission")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line("Type", "Experiment")
                        line("File List", "file-list_5-6.tsv")
                        line()
                        line("File", "file_5-6-1.txt")
                        line()
                        line("File", "file_5-6-2.txt")
                        line()
                    }.toString()

                val fileListVersion1 =
                    tsv {
                        line("Files", "Type")
                        line("file_5-6-3.txt", "Referenced 1")
                        line("file_5-6-4.txt", "Referenced 2")
                        line()
                    }.toString()

                webClient.uploadFile(tempFolder.createFile("file-list_5-6.tsv", fileListVersion1))
                webClient.uploadFile(tempFolder.createFile("file_5-6-1.txt", "5-6-1 file content"))
                webClient.uploadFile(tempFolder.createFile("file_5-6-2.txt", "5-6-2 file content"))
                webClient.uploadFile(tempFolder.createFile("file_5-6-3.txt", "5-6-3 file content"))
                webClient.uploadFile(tempFolder.createFile("file_5-6-4.txt", "5-6-4 file content"))

                assertThat(webClient.submitSingle(version1, TSV)).isSuccessful()

                val version2 =
                    tsv {
                        line("Submission", "S-RSTST6")
                        line("Title", "Public Submission Updated")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line("Type", "Experiment Updated")
                        line("File List", "file-list_5-6.tsv")
                        line()
                        line("File", "file_5-6-1.txt")
                        line("Type", "Exp File 1")
                        line()
                        line("File", "file_5-6-2.txt")
                        line("Type", "Exp File 2")
                        line()
                        line("Link", "CHEBI::19")
                        line()
                    }.toString()

                val fileListVersion2 =
                    tsv {
                        line("Files", "Type")
                        line("file_5-6-3.txt", "Referenced And Updated 1")
                        line("file_5-6-4.txt", "Referenced And Updated 2")
                        line()
                    }.toString()

                webClient.uploadFile(tempFolder.createOrReplaceFile("file-list_5-6.tsv", fileListVersion2))
                assertThat(webClient.submitSingle(version2, TSV)).isSuccessful()
            }
    }

    @Nested
    inner class ModifyPublicFiles {
        @Test
        fun `5-7 add files to public submission`() =
            runTest {
                val version1 =
                    tsv {
                        line("Submission", "S-RSTST7")
                        line("Title", "Add Submission Files")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line()
                        line("File", "file_5-7-1.txt")
                        line()
                    }.toString()

                webClient.uploadFile(tempFolder.createFile("file_5-7-1.txt", "5-7-1 file content"))
                assertThat(webClient.submitSingle(version1, TSV)).isSuccessful()

                val version2 =
                    tsv {
                        line("Submission", "S-RSTST7")
                        line("Title", "Update Submission Files")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line()
                        line("File", "file_5-7-1.txt")
                        line()
                        line("File", "file_5-7-2.txt")
                        line()
                    }.toString()

                webClient.uploadFile(tempFolder.createFile("file_5-7-2.txt", "5-7-2 file content"))
                assertThat(webClient.submitSingle(version2, TSV)).isSuccessful()
            }

        @Test
        fun `5-8 unauthorized user updates public submission files`() =
            runTest {
                val version1 =
                    tsv {
                        line("Submission", "S-RSTST8")
                        line("Title", "Update Submission Files")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line("File List", "file-list_5-8.tsv")
                        line()
                        line("File", "file_5-8-1.txt")
                        line()
                    }.toString()

                val fileListVersion1 =
                    tsv {
                        line("Files", "Type")
                        line("file_5-8-2.txt", "Referenced")
                        line()
                    }.toString()

                webClient.uploadFile(tempFolder.createFile("file-list_5-8.tsv", fileListVersion1))
                webClient.uploadFile(tempFolder.createFile("file_5-8-1.txt", "5-8-1 file content"))
                webClient.uploadFile(tempFolder.createFile("file_5-8-2.txt", "5-8-2 file content"))
                assertThat(webClient.submitSingle(version1, TSV)).isSuccessful()

                val version2 =
                    tsv {
                        line("Submission", "S-RSTST8")
                        line("Title", "Update Submission Files")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line("File List", "file-list_5-8.tsv")
                        line()
                        line("File", "file_5-8-1.txt")
                        line()
                    }.toString()

                webClient.uploadFile(tempFolder.createOrReplaceFile("file_5-8-1.txt", "5-8-1 file updated content"))
                webClient.uploadFile(tempFolder.createOrReplaceFile("file_5-8-2.txt", "5-8-2 file updated content"))
                webClient.submitAsync(version2, TSV)

                waitUntil(timeout = TWO_SECONDS) { requestRepository.getRequestStatus("S-RSTST8", 2) == INVALID }
            }

        @Test
        fun `5-9 authorized user updates public submission files`() =
            runTest {
                val version1 =
                    tsv {
                        line("Submission", "S-RSTST9")
                        line("Title", "Update Submission Files")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line()
                        line("File", "file_5-9-1.txt")
                        line()
                        line("File", "file_5-9-2.txt")
                        line()
                    }.toString()

                webClient.uploadFile(tempFolder.createFile("file_5-9-1.txt", "5-9-1 file content"))
                webClient.uploadFile(tempFolder.createFile("file_5-9-2.txt", "5-9-2 file content"))
                assertThat(webClient.submitSingle(version1, TSV)).isSuccessful()

                val version2 =
                    tsv {
                        line("Submission", "S-RSTST9")
                        line("Title", "Update Submission Files")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line()
                        line("File", "file_5-9-1.txt")
                        line()
                    }.toString()

                webClient.grantPermission(SuperUser.email, "S-RSTST9", UPDATE_PUBLIC.name)
                webClient.uploadFile(tempFolder.createOrReplaceFile("file_5-9-1.txt", "5-9-1 file updated content"))

                assertThat(webClient.submitSingle(version2, TSV)).isSuccessful()
            }

        @Test
        fun `5-10 unauthorized user removes file list`() =
            runTest {
                val version1 =
                    tsv {
                        line("Submission", "S-RSTST10")
                        line("Title", "Remove File List")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line("File List", "file-list_5-10.tsv")
                        line()
                        line("File", "file_5-10-1.txt")
                        line()
                    }.toString()

                val fileListVersion1 =
                    tsv {
                        line("Files", "Type")
                        line("file_5-10-2.txt", "Referenced")
                        line()
                    }.toString()

                webClient.uploadFile(tempFolder.createFile("file-list_5-10.tsv", fileListVersion1))
                webClient.uploadFile(tempFolder.createFile("file_5-10-1.txt", "5-10-1 file content"))
                webClient.uploadFile(tempFolder.createFile("file_5-10-2.txt", "5-10-2 file content"))
                assertThat(webClient.submitSingle(version1, TSV)).isSuccessful()

                val version2 =
                    tsv {
                        line("Submission", "S-RSTST10")
                        line("Title", "Remove File List")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line()
                        line("File", "file_5-10-1.txt")
                        line()
                    }.toString()

                webClient.submitAsync(version2, TSV)

                waitUntil(timeout = TWO_SECONDS) { requestRepository.getRequestStatus("S-RSTST10", 2) == INVALID }
            }
    }
}
