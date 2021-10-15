package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
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
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
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
                submission, JSON, listOf(fileList, tempFolder.createFile("File4.txt"))
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
                submission, JSON, listOf(fileList, tempFolder.createFile("File5.txt"))
            )

            assertThat(response).isSuccessful()
            assertSubmissionFiles("S-TEST5", "File5.txt")
            fileList.delete()
        }

        @Test
        fun `JSON submission with invalid file list format`() {
            val fileList = tempFolder.createFile("FileList.txt", "Invalid file list")
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
                .isThrownBy { webClient.submitSingle(submission, JSON, listOf(fileList)) }
                .withMessageContaining("Unsupported page tab format FileList.txt")
        }

        private fun assertSubmissionFiles(accNo: String, testFile: String) {
            val createdSub = submissionRepository.getExtByAccNo(accNo)
            val subFolder = "$submissionPath/${createdSub.relPath}"

            if (mongoMode) {
                if (enableFire) {
                    val submissionTabFiles = createdSub.pageTabFiles as List<FireFile>
                    assertThat(submissionTabFiles).hasSize(3)
                    assertThat(submissionTabFiles).isEqualTo(submissionFireTabFiles(accNo, subFolder))

                    val fileListTabFiles = createdSub.section.fileList!!.pageTabFiles as List<FireFile>
                    assertThat(fileListTabFiles).hasSize(3)
                    assertThat(fileListTabFiles).isEqualTo(fileListFireTabFiles(subFolder))
                } else {
                    val submissionTabFiles = createdSub.pageTabFiles as List<NfsFile>
                    assertThat(submissionTabFiles).hasSize(3)
                    assertThat(submissionTabFiles).isEqualTo(submissionNfsTabFiles(accNo, subFolder))

                    val fileListTabFiles = createdSub.section.fileList!!.pageTabFiles as List<NfsFile>
                    assertThat(fileListTabFiles).hasSize(3)
                    assertThat(fileListTabFiles).isEqualTo(fileListNfsTabFiles(subFolder))
                }
            }

            assertThat(Paths.get("$subFolder/Files/$testFile")).exists()
            assertThat(Paths.get("$subFolder/Files/FileList.xml")).exists()
            assertThat(Paths.get("$subFolder/Files/FileList.json")).exists()
            assertThat(Paths.get("$subFolder/Files/FileList.pagetab.tsv")).exists()

            assertThat(Paths.get("$subFolder/${createdSub.accNo}.xml")).exists()
            assertThat(Paths.get("$subFolder/${createdSub.accNo}.json")).exists()
            assertThat(Paths.get("$subFolder/${createdSub.accNo}.pagetab.tsv")).exists()
        }

        private fun submissionFireTabFiles(accNo: String, subFolder: String): List<FireFile> {
            val jsonFile = File("$subFolder/$accNo.json")
            val xmlFile = File("$subFolder/$accNo.xml")
            val tsvFile = File("$subFolder/$accNo.pagetab.tsv")
            return listOf(
                FireFile(
                    fileName = "$accNo.json",
                    filePath = "$accNo.json",
                    relPath = "$accNo.json",
                    fireId = "$accNo.json",
                    md5 = jsonFile.md5(),
                    size = jsonFile.size(),
                    attributes = listOf()
                ),
                FireFile(
                    fileName = "$accNo.xml",
                    filePath = "$accNo.xml",
                    relPath = "$accNo.xml",
                    fireId = "$accNo.xml",
                    md5 = xmlFile.md5(),
                    size = xmlFile.size(),
                    attributes = listOf()
                ),
                FireFile(
                    fileName = "$accNo.pagetab.tsv",
                    filePath = "$accNo.pagetab.tsv",
                    relPath = "$accNo.pagetab.tsv",
                    fireId = "$accNo.pagetab.tsv",
                    md5 = tsvFile.md5(),
                    size = tsvFile.size(),
                    attributes = listOf()
                )
            )
        }

        private fun fileListFireTabFiles(subFolder: String): List<FireFile> {
            val jsonFile = File("$subFolder/Files/FileList.json")
            val xmlFile = File("$subFolder/Files/FileList.xml")
            val tsvFile = File("$subFolder/Files/FileList.pagetab.tsv")
            return listOf(
                FireFile(
                    fileName = "FileList.json",
                    filePath = "FileList.json",
                    relPath = "Files/FileList.json",
                    fireId = "FileList.json",
                    md5 = jsonFile.md5(),
                    size = jsonFile.size(),
                    attributes = listOf()
                ),
                FireFile(
                    fileName = "FileList.xml",
                    filePath = "FileList.xml",
                    relPath = "Files/FileList.xml",
                    fireId = "FileList.xml",
                    md5 = xmlFile.md5(),
                    size = xmlFile.size(),
                    attributes = listOf()
                ),
                FireFile(
                    fileName = "FileList.pagetab.tsv",
                    filePath = "FileList.pagetab.tsv",
                    relPath = "Files/FileList.pagetab.tsv",
                    fireId = "FileList.pagetab.tsv",
                    md5 = tsvFile.md5(),
                    size = tsvFile.size(),
                    attributes = listOf()
                )
            )
        }

        private fun submissionNfsTabFiles(accNo: String, subFolder: String): List<NfsFile> = listOf(
            NfsFile(
                "$accNo.json",
                "$accNo.json",
                "$accNo.json",
                "$subFolder/$accNo.json",
                File(subFolder).resolve("$accNo.json")
            ),
            NfsFile(
                "$accNo.xml",
                "$accNo.xml",
                "$accNo.xml",
                "$subFolder/$accNo.xml",
                File(subFolder).resolve("$accNo.xml")
            ),
            NfsFile(
                "$accNo.pagetab.tsv",
                "$accNo.pagetab.tsv",
                "$accNo.pagetab.tsv",
                "$subFolder/$accNo.pagetab.tsv",
                File(subFolder).resolve("$accNo.pagetab.tsv")
            )
        )

        private fun fileListNfsTabFiles(subFolder: String): List<NfsFile> = listOf(
            NfsFile(
                "FileList.json",
                "FileList.json",
                "Files/FileList.json",
                "$subFolder/Files/FileList.json",
                File(subFolder).resolve("Files/FileList.json")
            ),
            NfsFile(
                "FileList.xml",
                "FileList.xml",
                "Files/FileList.xml",
                "$subFolder/Files/FileList.xml",
                File(subFolder).resolve("Files/FileList.xml")
            ),
            NfsFile(
                "FileList.pagetab.tsv",
                "FileList.pagetab.tsv",
                "Files/FileList.pagetab.tsv",
                "$subFolder/Files/FileList.pagetab.tsv",
                File(subFolder).resolve("Files/FileList.pagetab.tsv")
            )
        )
    }
}
