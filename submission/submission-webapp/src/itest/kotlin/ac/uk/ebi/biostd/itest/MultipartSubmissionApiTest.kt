package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.TestConfig
import ac.uk.ebi.biostd.itest.entities.GenericUser
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import arrow.core.Either
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.extensions.fileListName
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.redundent.kotlin.xml.xml
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Paths

@ExtendWith(TemporaryFolderExtension::class)
internal class MultipartSubmissionApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [TestConfig::class, SubmitterConfig::class, PersistenceConfig::class, FileConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Transactional
    @DirtiesContext
    inner class SingleSubmissionTest(@Autowired val submissionRepository: SubmissionRepository) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(RegisterRequest(GenericUser.email, GenericUser.username, GenericUser.password))
            webClient = securityClient.getAuthenticatedClient(GenericUser.username, GenericUser.password)
        }

        @Test
        fun `submit multipart JSON submission`() {
            val fileName = "DataFile1.txt"
            val accNo = "SimpleAcc1"

            val file = tempFolder.createFile(fileName)
            val submission = submission(accNo) {
                section(type = "Study") {
                    file(fileName)
                }
            }

            val response = webClient.submitSingle(submission, SubmissionFormat.JSON, listOf(file))
            assertSuccessfulResponse(response)

            val createdSubmission = submissionRepository.getExtendedByAccNo(accNo)
            assertThat(createdSubmission).hasAccNo(accNo)
            assertThat(createdSubmission.section.files).containsExactly(Either.left(File("DataFile1.txt")))

            val submissionFolderPath = "$basePath/submission/${createdSubmission.relPath}/Files"
            assertThat(Paths.get("$submissionFolderPath/$fileName")).exists()
        }

        @Test
        fun `submission with file list TSV`() {
            val submission = tsv {
                line("Submission", "S-TEST1")
                line("Title", "Test Submission")
                line()

                line("Study", "SECT-001")
                line("Title", "Root Section")
                line("File List", "FileList.tsv")
                line()
            }

            val fileList = tempFolder.createFile("FileList.tsv").apply {
                writeBytes(tsv {
                    line("Files", "GEN")
                    line("File1.txt", "ABC")
                }.toString().toByteArray())
            }

            val response = webClient.submitSingle(
                submission.toString(), SubmissionFormat.TSV, listOf(fileList, tempFolder.createFile("File1.txt")))

            assertSuccessfulResponse(response)
            assertSubmissionFiles("S-TEST1", "File1.txt")
        }

        @Test
        fun `submission with file list JSON`() {
            val submission = jsonObj {
                "accno" to "S-TEST2"
                "attributes" to jsonArray({
                    "name" to "Title"
                    "value" to "Test Submission"
                })
                "section" to {
                    "accno" to "SECT-001"
                    "type" to "Study"
                    "attributes" to jsonArray({
                        "name" to "Title"
                        "value" to "Root Section"
                    }, {
                        "name" to "File List"
                        "value" to "FileList.json"
                    })
                }
            }

            val fileList = tempFolder.createFile("FileList.json").apply {
                writeBytes(jsonArray({
                    "path" to "File2.txt"
                    "attributes" to jsonArray({
                        "name" to "GEN"
                        "value" to "ABC"
                    })
                }).toString().toByteArray())
            }

            val response = webClient.submitSingle(
                submission.toString(), SubmissionFormat.JSON, listOf(fileList, tempFolder.createFile("File2.txt")))

            assertSuccessfulResponse(response)
            assertSubmissionFiles("S-TEST2", "File2.txt")
        }

        @Test
        fun `submission with file list XML`() {
            val submission = xml("submission") {
                attribute("accno", "S-TEST3")
                "attributes" {
                    "attribute" {
                        "name" { -"Title" }
                        "value" { -"Test Submission" }
                    }
                }

                "section" {
                    attribute("accno", "SECT-001")
                    attribute("type", "Study")
                    "attributes" {
                        "attribute" {
                            "name" { -"Title" }
                            "value" { -"Root Section" }
                        }
                        "attribute" {
                            "name" { -"File List" }
                            "value" { -"FileList.xml" }
                        }
                    }
                }
            }

            val fileList = tempFolder.createFile("FileList.xml").apply {
                writeBytes(xml("table") {
                    "file" {
                        "path" { -"File3.txt" }
                        "attributes" {
                            "attribute" {
                                "name" { -"GEN" }
                                "value" { -"ABC" }
                            }
                        }
                    }
                }.toString().toByteArray())
            }

            val response = webClient.submitSingle(
                submission.toString(), SubmissionFormat.XML, listOf(fileList, tempFolder.createFile("File3.txt")))

            assertSuccessfulResponse(response)
            assertSubmissionFiles("S-TEST3", "File3.txt")
        }

        private fun <T> assertSuccessfulResponse(response: ResponseEntity<T>) {
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response.body).isNotNull
        }

        private fun assertSubmissionFiles(accNo: String, testFile: String) {
            val fileListName = "FileList"
            val createdSubmission = submissionRepository.getExtendedByAccNo(accNo, loadRefFiles = true)
            val submissionFolderPath = "$basePath/submission/${createdSubmission.relPath}"

            assertThat(createdSubmission.section.fileListName).isEqualTo(fileListName)
            assertThat(createdSubmission.extendedSection.fileList).isEqualTo(
                FileList(fileListName, listOf(File(testFile, attributes = listOf(Attribute("GEN", "ABC"))))))

            assertThat(Paths.get("$submissionFolderPath/Files/$testFile")).exists()

            assertThat(Paths.get("$submissionFolderPath/$fileListName.xml")).exists()
            assertThat(Paths.get("$submissionFolderPath/$fileListName.json")).exists()
            assertThat(Paths.get("$submissionFolderPath/$fileListName.pagetab.tsv")).exists()
        }
    }
}
