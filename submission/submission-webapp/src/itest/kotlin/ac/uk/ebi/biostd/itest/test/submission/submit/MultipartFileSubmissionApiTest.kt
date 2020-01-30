package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.XML
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.excel.excel
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.extensions.fileListName
import ebi.ac.uk.test.createFile
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
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Paths

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
    inner class SingleSubmissionTest(@Autowired private val submissionRepository: SubmissionRepository) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            webClient = getWebClient(serverPort, SuperUser)
        }

        @Test
        fun `submit excel submission`() {
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
                        cell("FileList.tsv")
                    }
                }
            }

            val fileList = tempFolder.createFile(
                "FileList.tsv",
                tsv {
                    line("Files", "GEN")
                    line("SomeFile.txt", "ABC")
                }.toString())

            val response = webClient.submitSingle(excelPageTab, listOf(fileList, tempFolder.createFile("SomeFile.txt")))
            assertThat(response).isSuccessful()
            assertSubmissionFiles("S-EXC123", "SomeFile.txt")
            fileList.delete()
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
            }.toString()

            val fileList = tempFolder.createFile(
                "FileList.tsv",
                tsv {
                    line("Files", "GEN")
                    line("File1.txt", "ABC")
                }.toString())

            val response = webClient.submitSingle(submission, TSV, listOf(fileList, tempFolder.createFile("File1.txt")))
            assertThat(response).isSuccessful()
            assertSubmissionFiles("S-TEST1", "File1.txt")
            fileList.delete()
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
            }.toString()

            val fileList = tempFolder.createFile(
                "FileList.json",
                jsonArray({
                    "path" to "File2.txt"
                    "attributes" to jsonArray({
                        "name" to "GEN"
                        "value" to "ABC"
                    })
                }).toString())

            val response = webClient.submitSingle(submission, JSON, listOf(fileList, tempFolder.createFile("File2.txt")))
            assertThat(response).isSuccessful()
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
                }.toString())

            val response = webClient.submitSingle(submission, XML, listOf(fileList, tempFolder.createFile("File3.txt")))
            assertThat(response).isSuccessful()
            assertSubmissionFiles("S-TEST3", "File3.txt")
        }

        private fun assertSubmissionFiles(accNo: String, testFile: String) {
            val fileListName = "FileList"
            val createdSubmission = submissionRepository.getExtendedByAccNo(accNo)
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
