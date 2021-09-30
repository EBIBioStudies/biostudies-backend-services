package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.excel.excel
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.nio.file.Paths

@ExtendWith(TemporaryFolderExtension::class)
internal class FileListSubmissionTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Transactional
    @DirtiesContext
    inner class MixedFormatFileListSubmissionTest(
        @Autowired private val securityTestService: SecurityTestService,
        @Autowired private val submissionRepository: SubmissionQueryService
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
        fun `JSON submission with TSV file list`() {
            val submission = jsonObj {
                "accno" to "S-TEST4"
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
                            "value" to "FileList.tsv"
                        }
                    )
                }
            }.toString()

            val fileList = tempFolder.createFile(
                "FileList.tsv",
                tsv {
                    line("Files", "GEN")
                    line("File4.txt", "ABC")
                }.toString()
            )

            val response = webClient.submitSingle(
                submission, SubmissionFormat.JSON, listOf(fileList, tempFolder.createFile("File4.txt"))
            )

            assertThat(response).isSuccessful()
            assertSubmissionFiles("S-TEST4", "File4.txt")
            fileList.delete()
        }

        @Test
        fun `JSON submission with XSL file list`() {
            val submission = jsonObj {
                "accno" to "S-TEST5"
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
                            "value" to "FileList.xlsx"
                        }
                    )
                }
            }.toString()

            val fileList = excel(File("${tempFolder.root.absolutePath}/FileList.xlsx")) {
                sheet("page tab") {
                    row {
                        cell("Files")
                        cell("GEN")
                    }
                    row {
                        cell("File5.txt")
                        cell("ABC")
                    }
                }
            }

            val response = webClient.submitSingle(
                submission, SubmissionFormat.JSON, listOf(fileList, tempFolder.createFile("File5.txt"))
            )

            assertThat(response).isSuccessful()
            assertSubmissionFiles("S-TEST5", "File5.txt")
            fileList.delete()
        }

        @Test
        fun `JSON submission with invalid file list format`() {
            val fileList = tempFolder.createFile("FileList.txt")
            val submission = jsonObj {
                "accno" to "S-TEST5"
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
                            "value" to "FileList.txt"
                        }
                    )
                }
            }.toString()

            assertThatExceptionOfType(WebClientException::class.java)
                .isThrownBy { webClient.submitSingle(submission, SubmissionFormat.JSON, listOf(fileList)) }
                .withMessageContaining("Unsupported page tab format FileList.txt")
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
