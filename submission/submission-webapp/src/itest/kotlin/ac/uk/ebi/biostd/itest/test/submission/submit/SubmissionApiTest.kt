package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.DefaultUser
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.factory.invalidLinkUrl
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.submission.ext.getSimpleByAccNo
import ebi.ac.uk.api.dto.UserRegistration
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.test.clean
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.collections.ifRight
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.nio.file.Files
import kotlin.test.assertFailsWith

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SubmissionApiTest(
        @Autowired val securityTestService: SecurityTestService,
        @Autowired val submissionRepository: SubmissionQueryService,
        @Autowired val sequenceRepository: SequenceDataRepository,
        @Autowired val tagsRefRepository: TagDataRepository,
        @Autowired val userDataRepository: UserDataRepository
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            securityTestService.registerUser(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)

            sequenceRepository.save(Sequence("S-BSST"))
            tagsRefRepository.save(DbTag(classifier = "classifier", name = "tag"))
        }

        @BeforeEach
        fun beforeEach() {
            tempFolder.clean()
        }

        @Test
        fun `submit with submission object`() {
            val submission = submission("SimpleAcc1") {
                title = "Simple Submission"
            }

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getSimpleByAccNo("SimpleAcc1")).isEqualTo(
                submission("SimpleAcc1") {
                    title = "Simple Submission"
                }
            )
        }

        @Test
        fun `empty accNo`() {
            val submission = tsv {
                line("Submission")
                line("Title", "Empty AccNo")
            }.toString()

            val response = webClient.submitSingle(submission, TSV)

            assertThat(response).isSuccessful()
            assertThat(submissionRepository.getSimpleByAccNo(response.body.accNo)).isEqualTo(
                submission(response.body.accNo) {
                    title = "Empty AccNo"
                }
            )
        }

        @Test
        fun `submission with root path`() {
            val submission = tsv {
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
                "RootPathFolder"
            )

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getSimpleByAccNo("S-12364")).isEqualTo(
                submission("S-12364") {
                    title = "Sample Submission"
                    rootPath = "RootPathFolder"
                    section("Study") { file("DataFile5.txt") }
                }
            )
        }

        @Test
        fun `re submit a submission with rootPath`() {
            val rootPath = "The-RootPath"
            val dataFile = "DataFile7.txt"

            val submission = tsv {
                line("Submission", "S-356789")
                line("Title", "Sample Submission")
                line("RootPath", rootPath)
                line()
                line("Study")
                line()
                line("File", "DataFile7.txt")
                line()
            }.toString()

            webClient.uploadFiles(listOf(tempFolder.createFile("DataFile7.txt")), rootPath)
            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

            webClient.deleteFile(dataFile, rootPath)

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
        }

        @Test
        fun `submission with on behalf another user`() {
            createUser(RegularUser, serverPort)

            val submission = tsv {
                line("Submission")
                line("Title", "Submission Title")
            }.toString()

            val onBehalfClient = SecurityWebClient
                .create("http://localhost:$serverPort")
                .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            val response = onBehalfClient.submitSingle(submission, TSV)
            assertThat(response).isSuccessful()

            val accNo = response.body.accNo
            assertThat(submissionRepository.getSimpleByAccNo(accNo)).isEqualTo(
                submission(accNo) {
                    title = "Submission Title"
                }
            )
        }

        @Test
        fun `submission with on behalf new user`() {
            val username = "Jhon doe"
            val email = "jhon@doe.email.com"

            val submission = tsv {
                line("Submission")
                line("Title", "Submission Title")
            }.toString()

            val response = webClient.submitSingle(submission, TSV, UserRegistration(username, email))
            val saved = submissionRepository.getExtByAccNo(response.body.accNo)

            assertThat(saved.owner).isEqualTo(email)
            assertThat(saved.submitter).isEqualTo(SuperUser.email)
            val newUser = userDataRepository.findByEmail(email)
            assertThat(newUser).isNotNull()
            assertThat(newUser!!.active).isFalse()
            assertThat(newUser!!.notificationsEnabled).isFalse()
        }

        @Test
        fun `submission with on behalf created user with files in its folders`() {
            val ownerUser = securityTestService.registerUser(RegularUser)

            Files.copy(
                tempFolder.createFile("ownerFile.txt").toPath(),
                ownerUser.magicFolder.path.resolve("ownerFile.txt")
            )
            webClient.uploadFile(tempFolder.createFile("submitterFile.txt"))

            val submission = tsv {
                line("Submission")
                line("Title", "Submission Title")
                line()

                line("Study")
                line()

                line("File", "ownerFile.txt")
                line()

                line("File", "submitterFile.txt")
                line()
            }.toString()

            val onBehalfClient = SecurityWebClient
                .create("http://localhost:$serverPort")
                .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            val response = onBehalfClient.submitSingle(submission, TSV)
            assertThat(response).isSuccessful()

            val subRelPath = submissionRepository.findExtByAccNo(response.body.accNo)?.relPath
            val filesFolder = tempFolder.root.resolve("submission/$subRelPath/Files")
            assertThat(filesFolder.resolve("ownerFile.txt")).exists()
            assertThat(filesFolder.resolve("submitterFile.txt")).exists()
        }

        @Test
        fun `submission with on behalf created user with the same file`() {
            val ownerUser = securityTestService.registerUser(RegularUser)

            Files.copy(
                tempFolder.createFile("ownerFile1.txt", "owner data").toPath(),
                ownerUser.magicFolder.path.resolve("file.txt")
            )
            webClient.uploadFile(tempFolder.createFile("file.txt", "submitter data"))

            val submission = tsv {
                line("Submission")
                line("Title", "Submission Title")
                line()

                line("Study")
                line()

                line("File", "file.txt")
                line()
            }.toString()

            val onBehalfClient = SecurityWebClient
                .create("http://localhost:$serverPort")
                .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            val response = onBehalfClient.submitSingle(submission, TSV)
            assertThat(response).isSuccessful()

            val subRelPath = submissionRepository.findExtByAccNo(response.body.accNo)?.relPath
            val testFile = tempFolder.root.resolve("submission/$subRelPath/Files/file.txt")
            assertThat(testFile).exists()
            assertThat(testFile).hasContent("submitter data")
        }

        @Test
        fun `submission with generic root section`() {
            val submission = tsv {
                line("Submission", "E-MTAB123")
                line("Title", "Generic Submission")
                line()

                line("Experiment")
                line()
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getSimpleByAccNo("E-MTAB123")).isEqualTo(
                submission("E-MTAB123") {
                    title = "Generic Submission"
                    section("Experiment") { }
                }
            )
        }

        @Test
        fun `submission with tags`() {
            val submission = tsv {
                line("Submission", "S-TEST123")
                line("Title", "Submission With Tags")
                line()

                line("Study", "SECT-001", "", "classifier:tag")
                line()
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getSimpleByAccNo("S-TEST123")).isEqualTo(
                submission("S-TEST123") {
                    title = "Submission With Tags"
                    section("Study") {
                        accNo = "SECT-001"
                        tags = mutableListOf(Pair("Classifier", "Tag"))
                    }
                }
            )
        }

        @Test
        fun `submission with group file`() {
            val groupName = "The-Group"
            val submission = tsv {
                line("Submission", "S-54896")
                line("Title", "Sample Submission")
                line()

                line("Study")
                line()

                line("File", "groups/$groupName/GroupFile1.txt")
                line()

                line("File", "groups/$groupName/folder/GroupFile2.txt")
                line()
            }.toString()

            webClient.addUserInGroup(webClient.createGroup(groupName, "group-desc").name, SuperUser.email)
            webClient.uploadGroupFiles(groupName, listOf(tempFolder.createFile("GroupFile1.txt")))
            webClient.uploadGroupFiles(groupName, listOf(tempFolder.createFile("GroupFile2.txt")), "folder")

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getSimpleByAccNo("S-54896")).isEqualTo(
                submission("S-54896") {
                    title = "Sample Submission"
                    section("Study") {
                        file("groups/$groupName/GroupFile1.txt")
                        file("groups/$groupName/folder/GroupFile2.txt")
                    }
                }
            )
        }

        @Test
        fun `resubmit existing submission`() {
            fun submission(accNo: String? = null) = tsv {
                if (accNo == null) line("Submission") else line("Submission", accNo)
                line("Title", "Simple Submission With Files 2")
                line("ReleaseDate", "2020-01-25")
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

            val fileListContent = tsv {
                line("Files", "Type")
                line("a/fileFileList.pdf", "inner")
                line("a", "folder")
            }.toString()

            webClient.uploadFiles(
                listOf(
                    tempFolder.createFile("fileSubSection.txt", "content"),
                    tempFolder.createFile("file-list.tsv", fileListContent),
                    tempFolder.createFile("file section.doc"),
                )
            )
            webClient.uploadFiles(listOf(tempFolder.createFile("fileFileList.pdf")), "a")

            val response = webClient.submitSingle(submission(), TSV)

            assertThat(response).isSuccessful()
            val accNo = response.body.accNo
            val submitted = submissionRepository.getExtByAccNo(accNo)
            assertThat(submitted.version).isEqualTo(1)
            assertThat(File("$submissionPath/${submitted.relPath}/Files/file section.doc")).exists()
            assertThat(File("$submissionPath/${submitted.relPath}/Files/fileSubSection.txt")).exists()
            assertThat(File("$submissionPath/${submitted.relPath}/Files/fileSubSection.txt")).hasContent("content")
            assertThat(File("$submissionPath/${submitted.relPath}/Files/a/fileFileList.pdf")).exists()

            val changedFile = tempFolder.root.resolve("fileSubSection.txt").apply { writeText("newContent") }
            webClient.uploadFiles(listOf(changedFile))

            val reSubmitResponse = webClient.submitSingle(submission(accNo), TSV)

            assertThat(reSubmitResponse).isSuccessful()
            val resubmitted = submissionRepository.getExtByAccNo(accNo)
            assertThat(resubmitted.version).isEqualTo(2)
            assertThat(File("$submissionPath/${resubmitted.relPath}/Files/file section.doc")).exists()
            assertThat(File("$submissionPath/${resubmitted.relPath}/Files/fileSubSection.txt")).exists()
            assertThat(File("$submissionPath/${resubmitted.relPath}/Files/fileSubSection.txt")).hasContent("newContent")
            assertThat(File("$submissionPath/${resubmitted.relPath}/Files/a/fileFileList.pdf")).exists()
        }

        @Test
        fun `resubmit existing submission with the same files`() {
            fun submission(accNo: String? = null) = tsv {
                if (accNo == null) line("Submission") else line("Submission", accNo)
                line("Title", "Simple Submission With Files 2")
                line("ReleaseDate", "2020-01-25")
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

            val fileListContent = tsv {
                line("Files", "Type")
                line("a/fileFileList.pdf", "inner")
                line("a", "folder")
            }.toString()

            webClient.uploadFiles(
                listOf(
                    tempFolder.createFile("fileSubSection.txt", "content"),
                    tempFolder.createFile("file-list.tsv", fileListContent),
                    tempFolder.createFile("file section.doc"),
                )
            )
            webClient.uploadFiles(listOf(tempFolder.createFile("fileFileList.pdf")), "a")

            val response = webClient.submitSingle(submission(), TSV)
            val accNo = response.body.accNo

            val submitted = submissionRepository.getExtByAccNo(accNo)
            assertThat(response).isSuccessful()
            assertThat(submitted.version).isEqualTo(1)
            assertThat(File("$submissionPath/${submitted.relPath}/Files/file section.doc")).exists()
            assertThat(File("$submissionPath/${submitted.relPath}/Files/fileSubSection.txt")).exists()
            assertThat(File("$submissionPath/${submitted.relPath}/Files/fileSubSection.txt")).hasContent("content")
            assertThat(File("$submissionPath/${submitted.relPath}/Files/a/fileFileList.pdf")).exists()

            val reSubmitResponse = webClient.submitSingle(submission(accNo), TSV)
            assertThat(reSubmitResponse).isSuccessful()
            val resubmitted = submissionRepository.getExtByAccNo(accNo)
            assertThat(resubmitted.version).isEqualTo(2)
            assertThat(File("$submissionPath/${resubmitted.relPath}/Files/file section.doc")).exists()
            assertThat(File("$submissionPath/${resubmitted.relPath}/Files/fileSubSection.txt")).exists()
            assertThat(File("$submissionPath/${resubmitted.relPath}/Files/fileSubSection.txt")).hasContent("content")
            assertThat(File("$submissionPath/${resubmitted.relPath}/Files/a/fileFileList.pdf")).exists()
        }

        @Test
        fun `new submission with past release date`() {
            val submission = tsv {
                line("Submission", "S-RLSD123")
                line("Title", "Test Public Submission")
                line("ReleaseDate", "2000-01-31")
                line()
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

            val savedSubmission = submissionRepository.getExtByAccNo("S-RLSD123")
            assertThat(savedSubmission.accNo).isEqualTo("S-RLSD123")
            assertThat(savedSubmission.title).isEqualTo("Test Public Submission")
            assertThat(savedSubmission.released).isTrue
        }

        @Test
        fun `new submission with empty accNo subsection table`() {
            val submission = tsv {
                line("Submission", "S-STBL123")
                line("Title", "Test Section Table")
                line()

                line("Study", "SECT-001")
                line()

                line("Data[SECT-001]", "Title")
                line("", "Group 1")
                line()
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

            val savedSubmission = submissionRepository.getExtByAccNo("S-STBL123")
            assertThat(savedSubmission.accNo).isEqualTo("S-STBL123")
            assertThat(savedSubmission.title).isEqualTo("Test Section Table")

            val section = savedSubmission.section
            assertThat(section.accNo).isEqualTo("SECT-001")
            assertThat(section.sections).hasSize(1)
            section.sections.first().ifRight {
                assertThat(it.sections).hasSize(1)

                val subSection = it.sections.first()
                assertThat(subSection.accNo).isEmpty()
                assertThat(subSection.attributes).hasSize(1)
                assertThat(subSection.attributes.first().name).isEqualTo("Title")
                assertThat(subSection.attributes.first().value).isEqualTo("Group 1")
            }
        }

        @Test
        fun `submit with invalid link Url`() {
            val exception = assertThrows(WebClientException::class.java) {
                webClient.submitSingle(invalidLinkUrl().toString(), TSV)
            }

            assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }

        @Test
        fun `submission with validation error`() {
            val submission = submission("S-400") {
                title = "Submission with invalid file"
                section("Study") { file("invalidfile.txt") }
            }

            val exception = assertFailsWith<WebClientException> {
                webClient.submitSingle(submission, SubmissionFormat.XML)
            }
            assertThat(exception.message!!.contains("Submission contains invalid files invalid file.txt"))
        }
    }
}
