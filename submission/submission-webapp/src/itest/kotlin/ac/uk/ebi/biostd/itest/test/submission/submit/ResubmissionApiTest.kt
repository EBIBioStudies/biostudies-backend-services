package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
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
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.createOrReplaceFile
import ebi.ac.uk.model.RequestStatus.INVALID
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Durations.TWO_SECONDS
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.time.OffsetDateTime

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ResubmissionApiTest(
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
    inner class ModifyPublicMetadata {
        @Test
        fun `5-4 modify metadata of a public submission`() =
            runTest {
                val version1 =
                    tsv {
                        line("Submission", "S-RSTST4")
                        line("Title", "Public Submission")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line("Type", "Experiment")
                        line("File List", "file-list_5-4.tsv")
                        line()
                        line("File", "file_5-4-1.txt")
                        line()
                        line("File", "file_5-4-2.txt")
                        line()
                    }.toString()

                val fileListVersion1 =
                    tsv {
                        line("Files", "Type")
                        line("file_5-4-3.txt", "Referenced 1")
                        line("file_5-4-4.txt", "Referenced 2")
                        line()
                    }.toString()

                webClient.uploadFile(tempFolder.createFile("file-list_5-4.tsv", fileListVersion1))
                webClient.uploadFile(tempFolder.createFile("file_5-4-1.txt", "5-4-1 file content"))
                webClient.uploadFile(tempFolder.createFile("file_5-4-2.txt", "5-4-2 file content"))
                webClient.uploadFile(tempFolder.createFile("file_5-4-3.txt", "5-4-3 file content"))
                webClient.uploadFile(tempFolder.createFile("file_5-4-4.txt", "5-4-4 file content"))

                assertThat(webClient.submitSingle(version1, TSV)).isSuccessful()

                val version2 =
                    tsv {
                        line("Submission", "S-RSTST4")
                        line("Title", "Public Submission Updated")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line("Type", "Experiment Updated")
                        line("File List", "file-list_5-4.tsv")
                        line()
                        line("File", "file_5-4-1.txt")
                        line("Type", "Exp File 1")
                        line()
                        line("File", "file_5-4-2.txt")
                        line("Type", "Exp File 2")
                        line()
                        line("Link", "CHEBI::19")
                        line()
                    }.toString()

                val fileListVersion2 =
                    tsv {
                        line("Files", "Type")
                        line("file_5-4-3.txt", "Referenced And Updated 1")
                        line("file_5-4-4.txt", "Referenced And Updated 2")
                        line()
                    }.toString()

                webClient.uploadFile(tempFolder.createOrReplaceFile("file-list_5-4.tsv", fileListVersion2))
                assertThat(webClient.submitSingle(version2, TSV)).isSuccessful()
            }
    }

    @Nested
    inner class ModifyPublicFiles {
        @Test
        fun `5-5 add files to public submission`() =
            runTest {
                val version1 =
                    tsv {
                        line("Submission", "S-RSTST5")
                        line("Title", "Add Submission Files")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line()
                        line("File", "file_5-5-1.txt")
                        line()
                    }.toString()

                webClient.uploadFile(tempFolder.createFile("file_5-5-1.txt", "5-5-1 file content"))
                assertThat(webClient.submitSingle(version1, TSV)).isSuccessful()

                val version2 =
                    tsv {
                        line("Submission", "S-RSTST5")
                        line("Title", "Update Submission Files")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line()
                        line("File", "file_5-5-1.txt")
                        line()
                        line("File", "file_5-5-2.txt")
                        line()
                    }.toString()

                webClient.uploadFile(tempFolder.createFile("file_5-5-2.txt", "5-5-2 file content"))
                assertThat(webClient.submitSingle(version2, TSV)).isSuccessful()
            }

        @Test
        fun `5-6 unauthorized user updates public submission files`() =
            runTest {
                val version1 =
                    tsv {
                        line("Submission", "S-RSTST6")
                        line("Title", "Update Submission Files")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line("File List", "file-list_5-6.tsv")
                        line()
                        line("File", "file_5-6-1.txt")
                        line()
                    }.toString()

                val fileListVersion1 =
                    tsv {
                        line("Files", "Type")
                        line("file_5-6-2.txt", "Referenced")
                        line()
                    }.toString()

                webClient.uploadFile(tempFolder.createFile("file-list_5-6.tsv", fileListVersion1))
                webClient.uploadFile(tempFolder.createFile("file_5-6-1.txt", "5-6-1 file content"))
                webClient.uploadFile(tempFolder.createFile("file_5-6-2.txt", "5-6-2 file content"))
                assertThat(webClient.submitSingle(version1, TSV)).isSuccessful()

                val version2 =
                    tsv {
                        line("Submission", "S-RSTST6")
                        line("Title", "Update Submission Files")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line("File List", "file-list_5-6.tsv")
                        line()
                        line("File", "file_5-6-1.txt")
                        line()
                    }.toString()

                webClient.uploadFile(tempFolder.createOrReplaceFile("file_5-6-1.txt", "5-6-1 file updated content"))
                webClient.uploadFile(tempFolder.createOrReplaceFile("file_5-6-2.txt", "5-6-2 file updated content"))
                webClient.submitAsync(version2, TSV)

                waitUntil(timeout = TWO_SECONDS) { requestRepository.getRequestStatus("S-RSTST6", 2) == INVALID }
            }

        @Test
        fun `5-7 authorized user updates public submission files`() =
            runTest {
                val version1 =
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

                webClient.uploadFile(tempFolder.createFile("file_5-7-1.txt", "5-7-1 file content"))
                webClient.uploadFile(tempFolder.createFile("file_5-7-2.txt", "5-7-2 file content"))
                assertThat(webClient.submitSingle(version1, TSV)).isSuccessful()

                val sub = submissionRepository.getExtByAccNo("S-RSTST7")
                assertThat(sub.version).isEqualTo(1)
                assertThat(File("$submissionPath/${sub.relPath}/Files/file_5-7-1.txt")).exists()
                assertThat(File("$submissionPath/${sub.relPath}/Files/file_5-7-2.txt")).exists()

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
                    }.toString()

                webClient.grantPermission(SuperUser.email, "S-RSTST7", UPDATE_PUBLIC.name)
                webClient.uploadFile(tempFolder.createOrReplaceFile("file_5-7-1.txt", "5-7-1 file updated content"))

                assertThat(webClient.submitSingle(version2, TSV)).isSuccessful()

                val subV2 = submissionRepository.getExtByAccNo("S-RSTST7")
                assertThat(subV2.version).isEqualTo(2)
                assertThat(File("$submissionPath/${sub.relPath}/Files/file_5-7-1.txt")).exists()
                assertThat(File("$submissionPath/${sub.relPath}/Files/file_5-7-2.txt")).doesNotExist()
            }

        @Test
        fun `5-8 unauthorized user removes file list`() =
            runTest {
                val version1 =
                    tsv {
                        line("Submission", "S-RSTST8")
                        line("Title", "Remove File List")
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
                        line("Title", "Remove File List")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line()
                        line("File", "file_5-8-1.txt")
                        line()
                    }.toString()

                webClient.submitAsync(version2, TSV)

                waitUntil(timeout = TWO_SECONDS) { requestRepository.getRequestStatus("S-RSTST8", 2) == INVALID }
            }
    }
}
