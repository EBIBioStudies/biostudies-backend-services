package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.enableFire
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.dsl.excel.excel
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.util.collections.second
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.nio.file.Paths

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class MultipartFileSubmissionApiTest(
    @Autowired private val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired private val securityTestService: SecurityTestService,
    @Autowired private val toSubmissionMapper: ToSubmissionMapper,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

    @Test
    fun `9-1 XLS submission`() =
        runTest {
            val excelPageTab =
                excel(File("${tempFolder.absolutePath}/ExcelSubmission.xlsx")) {
                    sheet("page tab") {
                        row {
                            cell("Submission")
                            cell("S-EXC123")
                        }
                        row {
                            cell("Title")
                            cell("Excel Submission")
                        }

                        row {
                            cell("")
                            cell("")
                        }
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

            val fileList =
                excel(File("${tempFolder.absolutePath}/FileList.xlsx")) {
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
            val config = SubmissionFilesConfig(listOf(fileList, tempFolder.createFile("SomeFile.txt")), storageMode)

            val response = webClient.submitSingle(excelPageTab, config)

            assertThat(response).isSuccessful()
            assertSubmissionFiles("S-EXC123", "SomeFile.txt")
            fileList.delete()
        }

    @Test
    fun `9-1-2 XLS submission with line break`() =
        runTest {
            val excelPageTab =
                excel(File("${tempFolder.absolutePath}/ExcelSubmission-2.xlsx")) {
                    sheet("page tab") {
                        row {
                            cell("Submission")
                            cell("S-EXC124")
                        }
                        row {
                            cell("Title")
                            cell("Excel \n Submission")
                        }
                        row {
                            cell("")
                            cell("")
                        }
                        row {
                            cell("Study")
                            cell("SECT-1")
                        }
                    }
                }

            val response = webClient.submitSingle(excelPageTab, SubmissionFilesConfig(emptyList(), storageMode))
            assertThat(response).isSuccessful()

            val sub = submissionRepository.getExtByAccNo(response.body.accNo)
            assertThat(Paths.get("$submissionPath/${sub.relPath}/${sub.accNo}.tsv")).hasContent(
                """
                Submission	S-EXC124
                Title	"Excel 
                 Submission"

                Study	SECT-1
                """.trimIndent(),
            )
        }

    @Test
    fun `9-2 TSV submission`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-TEST1")
                    line("Title", "Test Submission")
                    line()

                    line("Study", "SECT-001")
                    line("Title", "Root Section")
                    line("File List", "FileList.tsv")
                    line()
                }.toString()

            val fileList =
                tempFolder.createFile(
                    "FileList.tsv",
                    tsv {
                        line("Files", "GEN")
                        line("File1.txt", "ABC")
                    }.toString(),
                )

            val filesConfig = SubmissionFilesConfig(listOf(fileList, tempFolder.createFile("File1.txt")), storageMode)
            val response = webClient.submitSingle(submission, TSV, filesConfig)
            assertThat(response).isSuccessful()
            assertSubmissionFiles("S-TEST1", "File1.txt")
            fileList.delete()
        }

    @Test
    fun `9-3 JSON submission`() =
        runTest {
            val submission =
                jsonObj {
                    "accno" to "S-TEST2"
                    "attributes" to
                        jsonArray({
                            "name" to "Title"
                            "value" to "Test Submission"
                        })
                    "section" to {
                        "accno" to "SECT-001"
                        "type" to "Study"
                        "attributes" to
                            jsonArray(
                                {
                                    "name" to "Title"
                                    "value" to "Root Section"
                                },
                                {
                                    "name" to "File List"
                                    "value" to "FileList.json"
                                },
                            )
                    }
                }.toString()

            val fileList =
                tempFolder.createFile(
                    "FileList.json",
                    jsonArray({
                        "path" to "File2.txt"
                        "attributes" to
                            jsonArray({
                                "name" to "GEN"
                                "value" to "ABC"
                            })
                    }).toString(),
                )

            val filesConfig = SubmissionFilesConfig(listOf(fileList, tempFolder.createFile("File2.txt")), storageMode)
            val response = webClient.submitSingle(submission, JSON, filesConfig)
            assertThat(response).isSuccessful()
            assertSubmissionFiles("S-TEST2", "File2.txt")
            fileList.delete()
        }

    @Test
    fun `9-4 direct submission with overriden attributes`() =
        runTest {
            val submission =
                tempFolder.createFile(
                    "submission.tsv",
                    tsv {
                        line("Submission", "S-TEST6")
                        line("Title", "Test Submission")
                        line("Type", "Test")
                        line()

                        line("Study", "SECT-001")
                        line("Title", "Root Section")
                        line()
                    }.toString(),
                )

            val filesConfig = SubmissionFilesConfig(emptyList(), storageMode)
            val response =
                webClient.submitSingle(
                    sub = submission,
                    config = filesConfig,
                    attrs = hashMapOf(("Type" to "Exp"), ("Exp" to "1")),
                )
            assertThat(response).isSuccessful()
            submission.delete()

            val savedSubmission = toSubmissionMapper.toSimpleSubmission(submissionRepository.getExtByAccNo("S-TEST6"))
            assertThat(savedSubmission.attributes).hasSize(3)
            assertThat(savedSubmission["Exp"]).isEqualTo("1")
            assertThat(savedSubmission["Type"]).isEqualTo("Exp")
            assertThat(savedSubmission["Title"]).isEqualTo("Test Submission")
        }

    @Test
    fun `9-5 invalid format file`() =
        runTest {
            val submission = tempFolder.createFile("submission.txt", "invalid file")
            val filesConfig = SubmissionFilesConfig(emptyList(), storageMode)

            val exception = assertThrows<WebClientException> { webClient.submitSingle(submission, filesConfig) }
            assertThat(exception).hasMessageContaining("Unsupported page tab format submission.txt")
        }

    private suspend fun assertSubmissionFiles(
        accNo: String,
        testFile: String,
    ) {
        val createdSub = submissionRepository.getExtByAccNo(accNo)
        val subFolder = "$submissionPath/${createdSub.relPath}"

        if (enableFire) {
            assertFireSubFiles(createdSub, accNo, subFolder)
            assertFireFileListFiles(createdSub, subFolder)
        } else {
            val submissionTabFiles = createdSub.pageTabFiles
            assertThat(submissionTabFiles).hasSize(2)
            assertThat(submissionTabFiles).isEqualTo(submissionNfsTabFiles(accNo, subFolder))

            val fileListTabFiles = createdSub.section.fileList!!.pageTabFiles
            assertThat(fileListTabFiles).hasSize(2)
            assertThat(fileListTabFiles).isEqualTo(fileListNfsTabFiles(subFolder))
        }

        assertThat(Paths.get("$subFolder/Files/$testFile")).exists()
        assertThat(Paths.get("$subFolder/Files/FileList.json")).exists()
        assertThat(Paths.get("$subFolder/Files/FileList.tsv")).exists()

        assertThat(Paths.get("$subFolder/${createdSub.accNo}.json")).exists()
        assertThat(Paths.get("$subFolder/${createdSub.accNo}.tsv")).exists()
    }

    private fun assertFireSubFiles(
        submission: ExtSubmission,
        accNo: String,
        subFolder: String,
    ) {
        val submissionTabFiles = submission.pageTabFiles
        assertThat(submissionTabFiles).hasSize(2)

        val jsonTabFile = submissionTabFiles.first() as FireFile
        val jsonFile = File("$subFolder/$accNo.json")
        assertThat(jsonTabFile.filePath).isEqualTo("$accNo.json")
        assertThat(jsonTabFile.relPath).isEqualTo("$accNo.json")
        assertThat(jsonTabFile.fireId).isNotNull()
        assertThat(jsonTabFile.md5).isEqualTo(jsonFile.md5())
        assertThat(jsonTabFile.size).isEqualTo(jsonFile.size())

        val tsvTabFile = submissionTabFiles.second() as FireFile
        val tsvFile = File("$subFolder/$accNo.tsv")
        assertThat(tsvTabFile.filePath).isEqualTo("$accNo.tsv")
        assertThat(tsvTabFile.relPath).isEqualTo("$accNo.tsv")
        assertThat(tsvTabFile.fireId).isNotNull()
        assertThat(tsvTabFile.md5).isEqualTo(tsvFile.md5())
        assertThat(tsvTabFile.size).isEqualTo(tsvFile.size())
    }

    private fun assertFireFileListFiles(
        submission: ExtSubmission,
        subFolder: String,
    ) {
        val fileListTabFiles = submission.section.fileList!!.pageTabFiles
        assertThat(fileListTabFiles).hasSize(2)

        val jsonTabFile = fileListTabFiles.first() as FireFile
        val jsonFile = File("$subFolder/Files/FileList.json")
        assertThat(jsonTabFile.filePath).isEqualTo("FileList.json")
        assertThat(jsonTabFile.relPath).isEqualTo("Files/FileList.json")
        assertThat(jsonTabFile.fireId).isNotNull()
        assertThat(jsonTabFile.md5).isEqualTo(jsonFile.md5())
        assertThat(jsonTabFile.size).isEqualTo(jsonFile.size())

        val tsvTabFile = fileListTabFiles.second() as FireFile
        val tsvFile = File("$subFolder/Files/FileList.tsv")
        assertThat(tsvTabFile.filePath).isEqualTo("FileList.tsv")
        assertThat(tsvTabFile.relPath).isEqualTo("Files/FileList.tsv")
        assertThat(tsvTabFile.fireId).isNotNull()
        assertThat(tsvTabFile.md5).isEqualTo(tsvFile.md5())
        assertThat(tsvTabFile.size).isEqualTo(tsvFile.size())
    }

    private fun submissionNfsTabFiles(
        accNo: String,
        subFolder: String,
    ): List<NfsFile> {
        val jsonPath = "$subFolder/$accNo.json"
        val tsvPath = "$subFolder/$accNo.tsv"

        return listOf(
            createNfsFile("$accNo.json", "$accNo.json", File(jsonPath)),
            createNfsFile("$accNo.tsv", "$accNo.tsv", File(tsvPath)),
        )
    }

    private fun fileListNfsTabFiles(subFolder: String): List<NfsFile> {
        val jsonName = "FileList.json"
        val tsvName = "FileList.tsv"
        val jsonFile = File(subFolder).resolve("Files/$jsonName")
        val tsvFile = File(subFolder).resolve("Files/$tsvName")

        return listOf(
            createNfsFile(jsonName, "Files/$jsonName", jsonFile),
            createNfsFile(tsvName, "Files/$tsvName", tsvFile),
        )
    }
}
