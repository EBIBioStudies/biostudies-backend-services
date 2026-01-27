package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.common.TestMessageService
import ac.uk.ebi.biostd.itest.entities.FtpSuperUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.factory.invalidLinkUrl
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.ftpPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionRequestRepository
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.coroutines.waitForCompletion
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.model.RequestStatus.Companion.PROCESSED_STATUS
import ebi.ac.uk.model.RequestStatus.DRAFT
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Durations.FIVE_SECONDS
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import java.time.OffsetDateTime

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SubmissionApiTest(
    @param:Autowired val securityTestService: SecurityTestService,
    @param:Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @param:Autowired val toSubmissionMapper: ToSubmissionMapper,
    @param:Autowired val testMessageService: TestMessageService,
    @param:Autowired val submissionRequestRepository: SubmissionRequestRepository,
    @param:LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init(): Unit =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            securityTestService.ensureSequence("S-BSST")

            webClient = getWebClient(serverPort, SuperUser)
        }

    @Test
    fun `16-1 Submit study with submission object`() =
        runTest {
            val submission =
                submission("SimpleAcc1") {
                    title = "Simple Submission"
                }

            assertThat(webClient.submit(submission, TSV)).isSuccessful()
            assertThat(getSimpleSubmission("SimpleAcc1")).isEqualTo(
                submission("SimpleAcc1") {
                    title = "Simple Submission"
                },
            )
        }

    @Test
    fun `16-2 Submit study with empty accNo`() =
        runTest {
            val submission =
                tsv {
                    line("Submission")
                    line("Title", "Empty AccNo")
                }.toString()

            val response = webClient.submit(submission, TSV)

            assertThat(response).isSuccessful()
            assertThat(getSimpleSubmission(response.body.accNo)).isEqualTo(
                submission(response.body.accNo) {
                    title = "Empty AccNo"
                },
            )
        }

    @Test
    fun `16-3 Submit study using root path`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-12364")
                    line("Title", "Sample Submission")
                    line("RootPath", "RootPathFolder")
                    line()

                    line("Study")
                    line()

                    line("File", "DataFile5.txt")
                    line()
                }.toString()

            tempFolder.createDirectory("RootPathFolder")
            webClient.uploadFiles(
                listOf(tempFolder.createFile("RootPathFolder/DataFile5.txt", "An example content")),
                "RootPathFolder",
            )

            assertThat(webClient.submit(submission, TSV)).isSuccessful()
            assertThat(getSimpleSubmission("S-12364")).isEqualTo(
                submission("S-12364") {
                    title = "Sample Submission"
                    rootPath = "RootPathFolder"
                    section("Study") { file("DataFile5.txt") }
                },
            )
        }

    @Test
    fun `16-4 Submit study with generic root section`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "E-MTAB123")
                    line("Title", "Generic Submission")
                    line()

                    line("Experiment")
                    line()
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()
            assertThat(getSimpleSubmission("E-MTAB123")).isEqualTo(
                submission("E-MTAB123") {
                    title = "Generic Submission"
                    section("Experiment") { }
                },
            )
        }

    @Test
    fun `16-5 Submit study with invalid link Url`() =
        runTest {
            val exception =
                assertThrows<WebClientException> {
                    webClient.submit(invalidLinkUrl().toString(), TSV)
                }

            assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }

    @Test
    fun `16-6 Submit study with validation error`() =
        runTest {
            val submission =
                submission("S-400") {
                    title = "Submission with invalid file"
                    section("Study") { file("invalidfile.txt") }
                }

            val exception =
                assertThrows<WebClientException> {
                    webClient.submit(submission)
                }
            assertThat(exception.message!!.contains("Submission contains invalid files invalid file.txt"))
        }

    @Test
    @EnabledIfSystemProperty(
        named = "enableFire",
        matches = "false",
        disabledReason = "Test check ftp output folder which in Fire is a zip file.",
    )
    fun `16-7 Submit public study with folder make files public`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-500")
                    line("Title", "Submission")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()

                    line("Study")
                    line()

                    line("File", "folder1")
                    line()
                }.toString()

            webClient.createFolder("folder1")
            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val submitted = submissionRepository.getExtByAccNo("S-500")

            val ftpFiles = FileUtils.listAllFiles(File("$ftpPath/${submitted.relPath}/Files"))
            val expectedFolder = File("$ftpPath/${submitted.relPath}/Files/folder1")
            assertThat(ftpFiles).containsExactly(expectedFolder)
            assertThat(expectedFolder).isEmptyDirectory()
        }

    @Test
    fun `16-8 Submit public study with file make files public`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-600")
                    line("Title", "Submission")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()

                    line("Study")
                    line()

                    line("File", "file_16-8.txt")
                    line()
                }.toString()

            webClient.uploadFile(tempFolder.createFile("file_16-8.txt", "16-8 file content"))
            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val submitted = submissionRepository.getExtByAccNo("S-600")

            val ftpFiles = FileUtils.listAllFiles(File("$ftpPath/${submitted.relPath}/Files"))
            val expectedFile = File("$ftpPath/${submitted.relPath}/Files/file_16-8.txt")
            assertThat(ftpFiles).containsExactly(expectedFile)
            assertThat(expectedFile).hasContent("16-8 file content")
            assertThat(File("$submissionPath/${submitted.relPath}/Files/file_16-8.txt")).hasContent("16-8 file content")
        }

    @Test
    fun `16-9 Submit study not released makes files private`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-700")
                    line("Title", "Submission")
                    line("ReleaseDate", "2030-01-25")
                    line()

                    line("Study")
                    line()

                    line("File", "file_16-9.txt")
                    line()
                }.toString()

            webClient.uploadFile(tempFolder.createFile("file_16-9.txt", "16-9 file content"))
            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val submitted = submissionRepository.getExtByAccNo("S-700")

            assertThat(File("$ftpPath/${submitted.relPath}")).doesNotExist()
            val submissionFiles = FileUtils.listAllFiles(File("$submissionPath/${submitted.relPath}/Files"))
            val expectedFile = File("$submissionPath/${submitted.relPath}/Files/file_16-9.txt")
            assertThat(submissionFiles).containsOnly(expectedFile)
            assertThat(expectedFile).hasContent("16-9 file content")
        }

    @Test
    fun `16-10 Submit study with invalid characters file path`() =
        runTest {
            tempFolder.createDirectory("h_EglN1-Δβ2β3-GFP")
            tempFolder.createDirectory("h_EglN1-Δβ2β3-GFP/#4")

            val file1 = tempFolder.createFile("file_16-10.txt")
            val file2 = tempFolder.createFile("merged-%.tif")
            val submission =
                tsv {
                    line("Submission", "S-BSST1610")
                    line("Title", "Submission")
                    line("ReleaseDate", "2030-01-25")
                    line()

                    line("Study")
                    line()

                    line("Files")
                    line("file_16-10.txt")
                    line("h_EglN1-Δβ2β3-GFP/#4/merged-%.tif")
                    line()
                }.toString()

            webClient.uploadFiles(listOf(file1, file2))
            val exception = assertThrows<WebClientException> { webClient.submit(submission, TSV) }
            val request = submissionRequestRepository.getByAccNoAndStatusNotIn("S-BSST1610", PROCESSED_STATUS)
            val expectedError = "The given file path contains invalid characters: h_EglN1-Δβ2β3-GFP/#4/merged-%.tif"
            assertThat(exception).hasMessageContaining(expectedError)
            assertThat(request.status).isEqualTo(DRAFT)
            assertThat(request.errors).hasSize(1)
            assertThat(request.errors.first()).contains(expectedError)
        }

    @Test
    fun `16-10-1 Submit study with invalid characters file path in file list`() =
        runTest {
            tempFolder.createDirectory("h_EglN1-Δβ2β3-GFP")
            tempFolder.createDirectory("h_EglN1-Δβ2β3-GFP/#4")

            val file1 = tempFolder.createFile("file_16-10.txt")
            val file2 = tempFolder.createFile("merged-%.tif")
            val fileList =
                tempFolder.createFile(
                    "file-list.tsv",
                    tsv {
                        line("Files", "Type")
                        line("file_16-10.txt", "A")
                        line("h_EglN1-Δβ2β3-GFP/#4/merged-%.tif", "B")
                    }.toString(),
                )
            val submission =
                tsv {
                    line("Submission", "S-BSST16101")
                    line("Title", "Submission")
                    line("ReleaseDate", "2030-01-25")
                    line()

                    line("Study")
                    line("File List", "file-list.tsv")
                    line()
                }.toString()

            webClient.uploadFiles(listOf(file1, file2, fileList))
            val exception = assertThrows<WebClientException> { webClient.submit(submission, TSV) }
            val request = submissionRequestRepository.getByAccNoAndStatusNotIn("S-BSST16101", PROCESSED_STATUS)
            val expectedError = "The given file path contains invalid characters: h_EglN1-Δβ2β3-GFP/#4/merged-%.tif"
            val expectedFileListError = "Referenced in file list: file-list.tsv"
            assertThat(exception).hasMessageContainingAll(expectedError, expectedFileListError)
            assertThat(request.status).isEqualTo(DRAFT)
            assertThat(request.errors).hasSize(1)
            assertThat(request.errors.first()).contains(expectedError, expectedFileListError)
        }

    @Test
    fun `16-11 Submit study containing folder with trailing slash`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-BSST1611")
                    line("Title", "Submission")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()

                    line("Study")
                    line()

                    line("File", "inner/directory/")
                    line()
                }.toString()

            val exception = assertThrows<WebClientException> { webClient.submit(submission, TSV) }
            assertThat(exception).hasMessageContainingAll(
                "The given file path contains invalid characters: inner/directory/",
                "For more information check https://www.ebi.ac.uk/bioimage-archive/help-file-list",
            )
        }

    @Test
    fun `16-12 Submit study containing filelist with invalid name`() =
        runTest {
            val fileList =
                tsv {
                    line("Files", "Type")
                    line("file.txt", "test")
                    line()
                }.toString()
            val submission =
                tsv {
                    line("Submission", "S-BSST1612")
                    line("Title", "Submission With Invalid File List")
                    line("ReleaseDate", "2030-01-25")
                    line()

                    line("Study")
                    line("File List", "MS%20Raw%20data%20figures.tsv")
                    line()
                }.toString()
            val file1 = tempFolder.createFile("file.txt")
            val file2 = tempFolder.createFile("MS%20Raw%20data%20figures.tsv", fileList)

            webClient.uploadFiles(listOf(file1, file2))

            val exception = assertThrows<WebClientException> { webClient.submit(submission, TSV) }
            assertThat(exception).hasMessageContaining(
                "The given file path contains invalid characters: MS%20Raw%20data%20figures.tsv",
            )
        }

    @Test
    fun `16-13 Submit study by Regular user with Ftp home directory`() =
        runTest {
            securityTestService.ensureUserRegistration(FtpSuperUser)
            val ftpUserWebClient = getWebClient(serverPort, FtpSuperUser)

            val file = tempFolder.createFile("fileListFtpFile.txt")
            ftpUserWebClient.createFolder("a-folder")
            ftpUserWebClient.uploadFile(file, "a-folder")

            val fileList =
                tempFolder.createFile(
                    "FileList-Ftp.tsv",
                    tsv {
                        line("Files")
                        line("a-folder/fileListFtpFile.txt")
                    }.toString(),
                )

            ftpUserWebClient.uploadFile(fileList)

            val simpleFile = tempFolder.createFile("simpleFtpFile.txt", "An example content")
            ftpUserWebClient.uploadFile(simpleFile)

            val submission =
                tsv {
                    line("Submission", "SFTP-1")
                    line("Title", "FTP user Submission")
                    line()

                    line("Study")
                    line("File List", "FileList-Ftp.tsv")
                    line()
                    line("File", "simpleFtpFile.txt")
                }.toString()

            val result = ftpUserWebClient.submit(submission, TSV)

            assertThat(result).isSuccessful()
        }

    @Nested
    @SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["app.subBasePath=base/path"])
    inner class SubmitWebBasePath(
        @LocalServerPort val serverPort: Int,
    ) {
        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            webClient = getWebClient(serverPort, SuperUser)
        }

        @Test
        fun `16-14 Submit study when the system has the basePath property configured`() =
            runTest {
                val submission =
                    tsv {
                        line("Submission", "S-12366")
                        line("Title", "Sample Submission")
                        line()

                        line("Study")
                        line()

                        line("File", "file12366.txt")
                        line()
                    }.toString()
                webClient.uploadFiles(listOf(tempFolder.createFile("file12366.txt", "An example content")))

                assertThat(webClient.submit(submission, TSV)).isSuccessful()

                val extSub = submissionRepository.getExtByAccNo("S-12366")
                assertThat(extSub.relPath).isEqualTo("base/path/S-/366/S-12366")
            }
    }

    @Nested
    @SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["app.asyncMode=true"])
    inner class SubmitAsyncFileValidation(
        @LocalServerPort val serverPort: Int,
    ) {
        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            webClient = getWebClient(serverPort, SuperUser)
        }

        @Test
        fun `16-18 Submit files with errros`() =
            runTest {
                val accNo = "ASYNC-123"
                val submission =
                    tsv {
                        line("Submission", accNo)
                        line()
                        line("Study")
                        line()
                        line("File", "missing_file.txt")
                    }.toString()

                val response = webClient.submitAsync(submission, TSV)

                waitForCompletion(timeout = FIVE_SECONDS) {
                    val result = webClient.getSubmission(response.accNo)
                    assertThat(result?.status).isEqualTo("INVALID")
                    assertThat(result?.errors).containsExactly(
                        """
                        The following files could not be found:
                          - missing_file.txt
                        List of available sources:
                          - Provided Db files
                          - Request files []
                          - biostudies-mgmt@ebi.ac.uk user files
                        """.trimIndent(),
                    )
                }

                webClient.uploadFile(tempFolder.createFile("missing_file.txt", "content"))
                assertThat(webClient.submitFromDraft(accNo)).isSuccessful()
                waitForCompletion(timeout = FIVE_SECONDS) {
                    val result = webClient.getSubmission(response.accNo)
                    assertThat(result?.status).isEqualTo("PROCESSED")
                    assertThat(result?.errors).isEmpty()
                }
            }
    }

    @Test
    fun `16-15 Submit study publish SubmissionSubmitted message`() =
        runTest {
            val accNo = "MESSAGE-123"
            val submission =
                tsv {
                    line("Submission", accNo)
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()
            assertThat(testMessageService.findSubmittedMessages(accNo)).isNotNull()
        }

    @Test
    fun `16-16 Submit study with silentMode does not publish SubmissionSubmitted message`() =
        runTest {
            val accNo = "NO_MESSAGE-123"
            val submission =
                tsv {
                    line("Submission", accNo)
                }.toString()

            assertThat(webClient.submit(submission, TSV, SubmitParameters(silentMode = true))).isSuccessful()
            assertThat(testMessageService.findSubmittedMessages(accNo)).isNull()
        }

    @Test
    fun `16-17 Submit study with singleJobMode`() =
        runTest {
            val accNo = "PROCESS_ALL-123"
            val submission =
                tsv {
                    line("Submission", accNo)
                    line()
                    line("Study")
                    line()
                    line("File", "DataFile.PROCESS_ALL.txt")
                }.toString()

            val file = tempFolder.createFile("DataFile.PROCESS_ALL.txt", "An example content")
            webClient.uploadFiles(listOf(file))
            assertThat(webClient.submit(submission, TSV, SubmitParameters(singleJobMode = true))).isSuccessful()
        }

    @Test
    fun `16-18 Submit private study inner folder can be listed`() {
        fun asserPermissions(
            path: Path,
            permissions: Set<PosixFilePermission>,
        ) {
            val filePermissions = Files.getPosixFilePermissions(path)
            assertThat(filePermissions).containsExactlyInAnyOrderElementsOf(permissions)
        }

        runTest {
            val accNo = "PERMISSIONS-18"
            val submission =
                tsv {
                    line("Submission", accNo)
                    line()
                    line("Study")
                    line()
                    line("File", "inner/DataFile.PROCESS_ALL.txt")
                }.toString()

            val file = tempFolder.createFile("DataFile.PROCESS_ALL.txt", "An example content")
            webClient.createFolder("inner")
            webClient.uploadFiles(listOf(file), relativePath = "inner")
            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val submitted = submissionRepository.getExtByAccNo(accNo)
            val subDir = Paths.get("$submissionPath/${submitted.relPath}")

            asserPermissions(subDir, RWXR_XR_X)
            asserPermissions(subDir.resolve(FILES_PATH), RWXR_XR_X)
            asserPermissions(subDir.resolve(FILES_PATH).resolve("inner"), RWXR_XR_X)
        }
    }

    private suspend fun getSimpleSubmission(accNo: String) =
        toSubmissionMapper.toSimpleSubmission(submissionRepository.getExtByAccNo(accNo))
}
