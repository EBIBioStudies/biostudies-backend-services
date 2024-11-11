package ac.uk.ebi.biostd.itest.test.filelist

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.enableFire
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.requestFilesPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.asserts.assertThatThrows
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.dsl.excel.excel
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.model.ExtAttribute
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
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.nio.file.Paths

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class FileListSubmissionTest(
    @Autowired private val securityTestService: SecurityTestService,
    @Autowired private val subRepository: SubmissionPersistenceQueryService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureSequence("S-BSST")
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

    @BeforeEach
    fun beforeEach() {
        FileUtils.cleanDirectory(tempFolder)
    }

    @Test
    fun `3-1 JSON submission with TSV file list`() =
        runTest {
            val submission =
                jsonObj {
                    "accno" to "S-TEST4"
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
                                    "value" to "FileList.tsv"
                                },
                            )
                    }
                }.toString()

            val fileList =
                tempFolder.createFile(
                    "FileList.tsv",
                    tsv {
                        line("Files", "GEN")
                        line("File4.txt", "ABC")
                    }.toString(),
                )

            val parameters = SubmitParameters(storageMode = storageMode)
            val files = listOf(fileList, tempFolder.createFile("File4.txt"))

            val response = webClient.submitMultipart(submission, JSON, parameters, files)

            assertThat(response).isSuccessful()
            assertSubmissionFiles(accNo = "S-TEST4", testFile = "File4.txt", fileListName = "FileList")
        }

    @Test
    fun `3-2 JSON submission with XSL file list`() =
        runTest {
            val submission =
                jsonObj {
                    "accno" to "S-TEST5"
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
                                    "value" to "FileList.xlsx"
                                },
                            )
                    }
                }.toString()

            val fileList =
                excel(File("${tempFolder.absolutePath}/FileList.xlsx")) {
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

            val parameters = SubmitParameters(storageMode = storageMode)
            val files = listOf(fileList, tempFolder.createFile("File5.txt"))

            val response = webClient.submitMultipart(submission, JSON, parameters, files)

            assertThat(response).isSuccessful()
            assertSubmissionFiles(accNo = "S-TEST5", testFile = "File5.txt", fileListName = "FileList")
        }

    @Test
    fun `3-3 JSON submission with invalid file list format`() =
        runTest {
            val fileList = tempFolder.createFile("FileList.txt", "Invalid file list")
            val submission =
                jsonObj {
                    "accno" to "S-TEST5"
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
                                    "value" to "FileList.txt"
                                },
                            )
                    }
                }.toString()

            val params = SubmitParameters(storageMode = storageMode)
            assertThatThrows<WebClientException> {
                webClient.submitMultipart(submission, JSON, params, listOf(fileList))
            }.hasMessageContaining("Unsupported page tab format FileList.txt")
        }

    @Test
    fun `3-4 Filelist Submission with files inside a folder`() =
        runTest {
            val referencedFile = tempFolder.createFile("referenced.txt")
            val submission =
                tsv {
                    line("Submission", "S-TEST6")
                    line("Title", "Submission With Inner File List")
                    line()

                    line("Study")
                    line("File List", "folder/inner-file-list.tsv")
                    line()
                }.toString()

            val fileList =
                tempFolder.createFile(
                    "inner-file-list.tsv",
                    tsv {
                        line("Files", "GEN")
                        line("referenced.txt", "ABC")
                    }.toString(),
                )

            webClient.uploadFile(fileList, "folder")
            val params = SubmitParameters(storageMode = storageMode)
            assertThat(webClient.submitMultipart(submission, TSV, params, listOf(referencedFile))).isSuccessful()

            val extSubmission = webClient.getExtByAccNo("S-TEST6")
            val referencedFiles = webClient.getReferencedFiles(extSubmission.section.fileList!!.filesUrl!!).files

            assertThat(referencedFiles).hasSize(1)
            val referenced = referencedFiles.first()
            assertThat(referenced.filePath).isEqualTo("referenced.txt")
            assertThat(referenced.relPath).isEqualTo("Files/referenced.txt")
            assertThat(referenced.attributes).isEqualTo(listOf(ExtAttribute("GEN", "ABC")))
            assertThat(referenced.md5).isEqualTo(referencedFile.md5())
        }

    @Test
    fun `3-5 Filelist Submission with files reusing previous version file list`() =
        runTest {
            val referencedFile = tempFolder.createFile("File7.txt", "file 7 content")

            fun submission(fileList: String) =
                tsv {
                    line("Submission", "S-TEST72")
                    line("Title", "Reuse Previous Version File List")
                    line()

                    line("Study")
                    line("File List", fileList)
                    line()
                }.toString()

            val fileList =
                tempFolder.createFile(
                    "reusable-file-list.tsv",
                    tsv {
                        line("Files", "GEN")
                        line("File7.txt", "ABC")
                    }.toString(),
                )

            val firstVersion = submission(fileList = "reusable-file-list.tsv")
            val params = SubmitParameters(storageMode = storageMode)
            val files = listOf(fileList, referencedFile)

            assertThat(webClient.submitMultipart(firstVersion, TSV, params, files)).isSuccessful()
            assertSubmissionFiles("S-TEST72", "File7.txt", "reusable-file-list")

            fileList.delete()

            val secondVersion = submission(fileList = "reusable-file-list.json")
            assertThat(webClient.submit(secondVersion, TSV)).isSuccessful()
            assertSubmissionFiles("S-TEST72", "File7.txt", "reusable-file-list")
        }

    @Test
    fun `3-8 Filelist Submission with empty accNo`() =
        runTest {
            val submission =
                tsv {
                    line("Submission")
                    line("Title", "Empty AccNo")
                    line()

                    line("Study")
                    line("File List", "empty-accNo-file-list.tsv")
                    line()
                }.toString()

            val fileList =
                tsv {
                    line("Files", "GEN")
                    line("File8.txt", "ABC")
                }.toString()

            webClient.uploadFiles(
                listOf(
                    tempFolder.createFile("File8.txt", "file 8 content"),
                    tempFolder.createFile("empty-accNo-file-list.tsv", fileList),
                ),
            )

            val response = webClient.submit(submission, TSV)
            assertThat(response).isSuccessful()

            val extSub = subRepository.getExtByAccNo(response.body.accNo)
            val requestFiles = File("$requestFilesPath/${extSub.accNo}/${extSub.version}")

            assertSubmissionFiles(extSub.accNo, "File8.txt", "empty-accNo-file-list")
            assertThat(requestFiles).exists()
            assertThat(requestFiles.listFiles()).anyMatch { it.name.startsWith("empty-accNo-file-list") }
        }

    @Nested
    inner class NegativeCases {
        @Test
        fun `3-6 Filelist Submission with an empty file list`() =
            runTest {
                val sub =
                    tsv {
                        line("Submission", "S-TEST8")
                        line("Title", "Empty File List")
                        line()

                        line("Study")
                        line("File List", "empty-file-list.tsv")
                        line()
                    }.toString()

                val fileList =
                    tempFolder.createFile(
                        "empty-file-list.tsv",
                        tsv {
                            line("Files", "GEN")
                        }.toString(),
                    )

                val params = SubmitParameters(storageMode = storageMode)
                val files = listOf(fileList)

                val exception = assertThrows<WebClientException> { webClient.submitMultipart(sub, TSV, params, files) }
                assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
                assertThat(exception).hasMessageContaining("A file list should contain at least one file")
            }

        @Test
        fun `3-7 Filelist Submission with a file list with an empty attribute name`() =
            runTest {
                val referencedFile = tempFolder.createFile("File9.txt", "file 9 content")
                val sub =
                    tsv {
                        line("Submission", "S-TEST9")
                        line("Title", "Empty Attribute Name")
                        line()

                        line("Study")
                        line("File List", "no-attr-name-file-list.tsv")
                        line()
                    }.toString()

                val fileList =
                    tempFolder.createFile(
                        "no-attr-name-file-list.tsv",
                        tsv {
                            line("Files", "GEN", "")
                            line("File9.txt", "ABC", "DEF")
                        }.toString(),
                    )

                val params = SubmitParameters(storageMode = storageMode)
                val files = listOf(fileList, referencedFile)
                val exception = assertThrows<WebClientException> { webClient.submitMultipart(sub, TSV, params, files) }
                assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
                assertThat(exception).hasMessageContaining("Attribute name is required")
            }
    }

    private suspend fun assertSubmissionFiles(
        accNo: String,
        testFile: String,
        fileListName: String,
    ) {
        val createdSub = subRepository.getExtByAccNo(accNo)
        val subFolder = "$submissionPath/${createdSub.relPath}"

        if (enableFire) {
            assertFireSubFiles(createdSub, accNo, subFolder)
            assertFireFileListFiles(createdSub, fileListName, subFolder)
        } else {
            val submissionTabFiles = createdSub.pageTabFiles
            assertThat(submissionTabFiles).hasSize(2)
            assertThat(submissionTabFiles).isEqualTo(submissionNfsTabFiles(accNo, subFolder))

            val fileListTabFiles = createdSub.section.fileList!!.pageTabFiles
            assertThat(fileListTabFiles).hasSize(2)
            assertThat(fileListTabFiles).isEqualTo(fileListNfsTabFiles(fileListName, subFolder))
        }

        assertThat(Paths.get("$subFolder/Files/$testFile")).exists()
        assertThat(Paths.get("$subFolder/Files/$fileListName.json")).exists()
        assertThat(Paths.get("$subFolder/Files/$fileListName.tsv")).exists()

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
        sub: ExtSubmission,
        fileListName: String,
        subFolder: String,
    ) {
        val fileListTabFiles = sub.section.fileList!!.pageTabFiles
        assertThat(fileListTabFiles).hasSize(2)

        val jsonTabFile = fileListTabFiles.first() as FireFile
        val jsonFile = File("$subFolder/Files/$fileListName.json")
        assertThat(jsonTabFile.filePath).isEqualTo("$fileListName.json")
        assertThat(jsonTabFile.relPath).isEqualTo("Files/$fileListName.json")
        assertThat(jsonTabFile.fireId).isNotNull()
        assertThat(jsonTabFile.md5).isEqualTo(jsonFile.md5())
        assertThat(jsonTabFile.size).isEqualTo(jsonFile.size())

        val tsvTabFile = fileListTabFiles.second() as FireFile
        val tsvFile = File("$subFolder/Files/$fileListName.tsv")
        assertThat(tsvTabFile.filePath).isEqualTo("$fileListName.tsv")
        assertThat(tsvTabFile.relPath).isEqualTo("Files/$fileListName.tsv")
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

    private fun fileListNfsTabFiles(
        fileListName: String,
        subFolder: String,
    ): List<NfsFile> {
        val jsonName = "$fileListName.json"
        val tsvName = "$fileListName.tsv"
        val jsonFile = File(subFolder).resolve("Files/$jsonName")
        val tsvFile = File(subFolder).resolve("Files/$tsvName")

        return listOf(
            createNfsFile(jsonName, "Files/$jsonName", jsonFile),
            createNfsFile(tsvName, "Files/$tsvName", tsvFile),
        )
    }
}
