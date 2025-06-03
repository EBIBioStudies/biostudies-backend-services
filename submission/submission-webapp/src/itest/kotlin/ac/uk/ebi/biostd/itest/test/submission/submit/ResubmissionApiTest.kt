package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.coroutines.waitForCompletion
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.createOrReplaceFile
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Durations.FIVE_SECONDS
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
import java.time.Duration
import java.time.OffsetDateTime

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ResubmissionApiTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val requestRepository: SubmissionRequestPersistenceService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired val filesRepository: SubmissionFilesPersistenceService,
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
        fun `5-1 Resubmit study updating a file content`() =
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
                assertThat(webClient.submit(submission, TSV)).isSuccessful()

                val sub = submissionRepository.getExtByAccNo("S-RSTST1")
                assertThat(sub.version).isEqualTo(1)
                assertThat(File("$submissionPath/${sub.relPath}/Files/file section.doc")).hasContent("doc content")
                assertThat(File("$submissionPath/${sub.relPath}/Files/fileSubSection.txt")).hasContent("content")
                assertThat(File("$submissionPath/${sub.relPath}/Files/a/fileFileList.pdf")).hasContent("pdf content")

                val v1ModificationTime = sub.modificationTime
                val changedFile = tempFolder.resolve("fileSubSection.txt").apply { writeText("newContent") }
                webClient.uploadFiles(listOf(changedFile))
                assertThat(webClient.submit(submission, TSV)).isSuccessful()

                val deprecatedVersion = submissionRepository.getExtByAccNoAndVersion("S-RSTST1", -1)
                assertThat(deprecatedVersion.modificationTime).isEqualTo(v1ModificationTime)

                val subV2 = submissionRepository.getExtByAccNo("S-RSTST1")
                assertThat(subV2.version).isEqualTo(2)
                assertThat(File("$submissionPath/${subV2.relPath}/Files/file section.doc")).exists()
                assertThat(File("$submissionPath/${subV2.relPath}/Files/fileSubSection.txt")).hasContent("newContent")
                assertThat(File("$submissionPath/${subV2.relPath}/Files/a/fileFileList.pdf")).exists()
            }

        @Test
        fun `5-2 Resubmit study with the same files`() =
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
                assertThat(webClient.submit(submission, TSV)).isSuccessful()

                val sub = submissionRepository.getExtByAccNo("S-RSTST2")
                assertThat(sub.version).isEqualTo(1)
                assertThat(File("$submissionPath/${sub.relPath}/Files/file section.doc")).exists()
                assertThat(File("$submissionPath/${sub.relPath}/Files/fileSubSection.txt")).exists()
                assertThat(File("$submissionPath/${sub.relPath}/Files/fileSubSection.txt")).hasContent("content")
                assertThat(File("$submissionPath/${sub.relPath}/Files/a/fileFileList.pdf")).exists()

                assertThat(webClient.submit(submission, TSV)).isSuccessful()
                val subV2 = submissionRepository.getExtByAccNo("S-RSTST2")
                assertThat(subV2.version).isEqualTo(2)
                assertThat(File("$submissionPath/${subV2.relPath}/Files/file section.doc")).exists()
                assertThat(File("$submissionPath/${subV2.relPath}/Files/fileSubSection.txt")).exists()
                assertThat(File("$submissionPath/${subV2.relPath}/Files/fileSubSection.txt")).hasContent("content")
                assertThat(File("$submissionPath/${subV2.relPath}/Files/a/fileFileList.pdf")).exists()
            }

        @Test
        fun `5-3 Resubmit study with rootPath`() =
            runTest {
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
                assertThat(webClient.submit(submission, TSV)).isSuccessful()

                webClient.deleteFile(dataFile, rootPath)

                assertThat(webClient.submit(submission, TSV)).isSuccessful()
            }
    }

    @Test
    fun `5-4 Resubmit study updating only metadata`() =
        runTest {
            suspend fun assertSubmission(
                title: String,
                sectionType: String,
                fileType: String,
                filelistFileType: String,
            ) {
                val submission = submissionRepository.getExtByAccNo("S-RSTST4", includeFileListFiles = true)
                assertThat(submission.title).isEqualTo(title)

                val section = submission.section
                assertThat(section.attributes).hasSize(1)
                val secAttribute = section.attributes[0]
                assertThat(secAttribute.name).isEqualTo("Type")
                assertThat(secAttribute.value).isEqualTo(sectionType)

                assertThat(section.files).hasSize(1)
                assertThat(section.files[0]).hasLeftValueSatisfying {
                    assertThat(it.attributes).hasSize(1)
                    val attribute = it.attributes[0]
                    assertThat(attribute.name).isEqualTo("Type")
                    assertThat(attribute.value).isEqualTo(fileType)
                }

                assertThat(section.fileList).isNotNull()
                val files = filesRepository.getReferencedFiles(submission, section.fileList!!.fileName).toList()
                assertThat(files).hasSize(1)

                val attributes = files[0].attributes
                assertThat(attributes).hasSize(1)
                assertThat(attributes[0].name).isEqualTo("Type")
                assertThat(attributes[0].value).isEqualTo(filelistFileType)
            }

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
                    line("Type", "File type")
                    line()
                    line("Link", "Link")
                }.toString()

            val fileListVersion1 =
                tsv {
                    line("Files", "Type")
                    line("file_5-4-2.txt", "Filelist file type")
                    line()
                }.toString()

            webClient.uploadFile(tempFolder.createFile("file-list_5-4.tsv", fileListVersion1))
            webClient.uploadFile(tempFolder.createFile("file_5-4-1.txt", "5-4-1 file content"))
            webClient.uploadFile(tempFolder.createFile("file_5-4-2.txt", "5-4-2 file content"))

            assertThat(webClient.submit(version1, TSV)).isSuccessful()
            assertSubmission(
                title = "Public Submission",
                sectionType = "Experiment",
                fileType = "File type",
                filelistFileType = "Filelist file type",
            )

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
                    line("Type", "File type Updated")
                    line()
                    line("Link", "Link Updated")
                    line()
                }.toString()

            val fileListVersion2 =
                tsv {
                    line("Files", "Type")
                    line("file_5-4-2.txt", "Filelist file type Updated")
                    line()
                }.toString()

            webClient.uploadFile(tempFolder.createOrReplaceFile("file-list_5-4.tsv", fileListVersion2))
            assertThat(webClient.submit(version2, TSV)).isSuccessful()
            assertSubmission(
                title = "Public Submission Updated",
                sectionType = "Experiment Updated",
                fileType = "File type Updated",
                filelistFileType = "Filelist file type Updated",
            )
        }

    @Test
    fun `5-5 Resubmit study adding new files`() =
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
            assertThat(webClient.submit(version1, TSV)).isSuccessful()

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
            assertThat(webClient.submit(version2, TSV)).isSuccessful()

            val submission = submissionRepository.getExtByAccNo("S-RSTST5", includeFileListFiles = true)
            val section = submission.section
            assertThat(section.files).hasSize(2)
            assertThat(section.files[0]).hasLeftValueSatisfying { assertThat(it.fileName).isEqualTo("file_5-5-1.txt") }
            assertThat(section.files[1]).hasLeftValueSatisfying { assertThat(it.fileName).isEqualTo("file_5-5-2.txt") }
        }

    @Test
    fun `5-6 Resubmit study currenlty being flag as invalid`() =
        runTest {
            val version1 =
                tsv {
                    line("Submission", "S-ACCNO56")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Study")
                    line()
                    line("File", "file_5_6_1.txt")
                    line()
                }.toString()

            webClient.uploadFile(tempFolder.createFile("file_5_6_1.txt", "5-6-1 file content"))
            assertThat(webClient.submit(version1, TSV)).isSuccessful()

            val version2 =
                tsv {
                    line("Submission", "S-ACCNO56")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Study")
                    line()
                    line("File", "file_5_6_2.txt")
                    line()
                }.toString()

            webClient.uploadFile(tempFolder.createFile("file_5_6_2.txt", "5-6-2 file content"))

            val (accNo, version) = webClient.submitAsync(version2, TSV)

            waitUntil(
                timeout = Duration.ofSeconds(10),
            ) { requestRepository.getRequest(accNo, version).status == RequestStatus.INVALID }

            val response = webClient.submitAsync(version2, TSV)
            waitForCompletion(timeout = FIVE_SECONDS) {
                val result = webClient.getSubmission(response.accNo)
                assertThat(result?.status).isEqualTo("INVALID")
                assertThat(result?.errors).containsExactly(
                    "File deletion/modifications require admin permission.",
                )
            }
        }
}
