package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.XML
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.submission.ext.getSimpleByAccNo
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.excel.excel
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.redundent.kotlin.xml.xml
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Paths

// TODO Fix all integration tests
@ExtendWith(TemporaryFolderExtension::class)
internal class MultipartFileSubmissionApiTest(
    private val tempFolder: TemporaryFolder
) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Transactional
    @DirtiesContext
    inner class SingleSubmissionTest(
        @Autowired private val submissionRepository: SubmissionQueryService,
        @Autowired private val securityTestService: SecurityTestService
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            securityTestService.registerUser(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

        @Test
        fun `XLS submission`() {
            val excelPageTab = excel("${tempFolder.root.absolutePath}/ExcelSubmission.xlsx") {
                sheet("page tab") {
                    row {
                        cell("Submission")
                        cell("S-EXC123")
                    }
                    row {
                        cell("Title")
                        cell("Excel Submission")
                    }

                    emptyRow()

                    row {
                        cell("Study")
                        cell("SECT-001")
                    }
                    row {
                        cell("Title")
                        cell("Root Section")
                    }
                    row {
                        cell("File List")
                        cell("FileList.xlsx")
                    }
                }
            }

            val fileList = excel("${tempFolder.root.absolutePath}/FileList.xlsx") {
                sheet("page tab") {
                    row {
                        cell("Files")
                        cell("GEN")
                    }
                    row {
                        cell("SomeFile.txt")
                        cell("ABC")
                    }
                }
            }

            val response = webClient.submitSingle(excelPageTab, listOf(fileList, tempFolder.createFile("SomeFile.txt")))
            assertThat(response).isSuccessful()
            assertSubmissionFiles("S-EXC123", "SomeFile.txt")
            fileList.delete()
        }

        @Test
        fun `TSV submission`() {
            val submission = tsv {
                line("Submission", "S-TEST1")
                line("Title", "Test Submission")
                line()

                line("Study", "SECT-001")
                line("Title", "Root Section")
                line("File List", "FileList.tsv")
                line()
            }.toString()

            val fileList = tempFolder.createFile(
                "FileList.tsv",
                tsv {
                    line("Files", "GEN")
                    line("File1.txt", "ABC")
                }.toString()
            )

            val response = webClient.submitSingle(submission, TSV, listOf(fileList, tempFolder.createFile("File1.txt")))
            assertThat(response).isSuccessful()
            assertSubmissionFiles("S-TEST1", "File1.txt")
            fileList.delete()
        }

        @Test
        fun `JSON submission`() {
            val submission = jsonObj {
                "accno" to "S-TEST2"
                "attributes" to jsonArray({
                    "name" to "Title"
                    "value" to "Test Submission"
                })
                "section" to {
                    "accno" to "SECT-001"
                    "type" to "Study"
                    "attributes" to jsonArray(
                        {
                            "name" to "Title"
                            "value" to "Root Section"
                        },
                        {
                            "name" to "File List"
                            "value" to "FileList.json"
                        }
                    )
                }
            }.toString()

            val fileList = tempFolder.createFile(
                "FileList.json",
                jsonArray({
                    "path" to "File2.txt"
                    "attributes" to jsonArray({
                        "name" to "GEN"
                        "value" to "ABC"
                    })
                }).toString()
            )

            val response = webClient.submitSingle(submission, JSON, listOf(fileList, tempFolder.createFile("File2.txt")))
            assertThat(response).isSuccessful()
            assertSubmissionFiles("S-TEST2", "File2.txt")
            fileList.delete()
        }

        @Test
        fun `XML submission`() {
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
            }.toString()

            val fileList = tempFolder.createFile(
                "FileList.xml",
                xml("table") {
                    "file" {
                        "path" { -"File3.txt" }
                        "attributes" {
                            "attribute" {
                                "name" { -"GEN" }
                                "value" { -"ABC" }
                            }
                        }
                    }
                }.toString()
            )

            val response = webClient.submitSingle(submission, XML, listOf(fileList, tempFolder.createFile("File3.txt")))
            assertThat(response).isSuccessful()
            assertSubmissionFiles("S-TEST3", "File3.txt")
            fileList.delete()
        }

        @Test
        fun `direct submission with overriden attributes`() {
            val submission = tempFolder.createFile(
                "submission.tsv",
                tsv {
                    line("Submission", "S-TEST6")
                    line("Title", "Test Submission")
                    line("Type", "Test")
                    line()

                    line("Study", "SECT-001")
                    line("Title", "Root Section")
                    line()
                }.toString()
            )

            val response = webClient.submitSingle(submission, emptyList(), hashMapOf(("Type" to "Exp"), ("Exp" to "1")))
            assertThat(response).isSuccessful()
            submission.delete()

            val savedSubmission = submissionRepository.getSimpleByAccNo("S-TEST6")
            assertThat(savedSubmission.attributes).hasSize(3)
            assertThat(savedSubmission["Exp"]).isEqualTo("1")
            assertThat(savedSubmission["Type"]).isEqualTo("Exp")
            assertThat(savedSubmission["Title"]).isEqualTo("Test Submission")
        }

        @Test
        fun `invalid format file`() {
            val submission = tempFolder.createFile("submission.txt")

            assertThatExceptionOfType(WebClientException::class.java)
                .isThrownBy { webClient.submitSingle(submission, emptyList()) }
                .withMessageContaining("Unsupported page tab format submission.txt")
        }

        private fun assertSubmissionFiles(accNo: String, testFile: String) {
            val fileListName = "FileList"
            val createdSubmission = submissionRepository.getExtByAccNo(accNo)
            val submissionFolderPath = "$submissionPath/${createdSubmission.relPath}"

            assertThat(createdSubmission.section.fileList?.fileName).isEqualTo(fileListName)
            assertThat(createdSubmission.section.fileList).isEqualTo(ExtFileList(fileListName, emptyList()))

            assertThat(Paths.get("$submissionFolderPath/Files/$testFile")).exists()
            assertThat(Paths.get("$submissionFolderPath/Files/$fileListName.xml")).exists()
            assertThat(Paths.get("$submissionFolderPath/Files/$fileListName.json")).exists()
            assertThat(Paths.get("$submissionFolderPath/Files/$fileListName.pagetab.tsv")).exists()
        }
    }
}
