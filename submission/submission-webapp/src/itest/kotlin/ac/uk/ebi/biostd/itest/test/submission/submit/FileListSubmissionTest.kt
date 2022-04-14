package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
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
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
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

        @Test
        fun `list referenced files`() {
            val referencedFile = tempFolder.createFile("referenced.txt")
            val submission = tsv {
                line("Submission", "S-TEST6")
                line("Title", "Submission With Inner File List")
                line()

                line("Study")
                line("File List", "folder/inner-file-list.tsv")
                line()
            }.toString()

            val fileList = tempFolder.createFile(
                "inner-file-list.tsv",
                tsv {
                    line("Files", "GEN")
                    line("referenced.txt", "ABC")
                }.toString()
            )

            webClient.uploadFile(fileList, "folder")
            assertThat(webClient.submitSingle(submission, TSV, listOf(referencedFile))).isSuccessful()

            val extSubmission = webClient.getExtByAccNo("S-TEST6")
            val referencedFiles = webClient.getReferencedFiles(extSubmission.section.fileList!!.filesUrl!!).files

            assertThat(referencedFiles).hasSize(1)
            val referenced = referencedFiles.first()
            assertThat(referenced.filePath).isEqualTo("referenced.txt")
            assertThat(referenced.relPath).isEqualTo("Files/referenced.txt")
            assertThat(referenced.attributes).isEqualTo(listOf(ExtAttribute("GEN", "ABC")))
            assertThat(referenced.md5).isEqualTo(referencedFile.md5())
        }

        private fun assertSubmissionFiles(accNo: String, testFile: String) {
            val createdSub = submissionRepository.getExtByAccNo(accNo)
            val subFolder = "$submissionPath/${createdSub.relPath}"

            if (enableFire) {
                assertFireSubFiles(createdSub, accNo, subFolder)
                assertFireFileListFiles(createdSub, accNo, subFolder)
            } else {
                val submissionTabFiles = createdSub.pageTabFiles as List<NfsFile>
                assertThat(submissionTabFiles).hasSize(3)
                assertThat(submissionTabFiles).isEqualTo(submissionNfsTabFiles(accNo, subFolder))

                val fileListTabFiles = createdSub.section.fileList!!.pageTabFiles as List<NfsFile>
                assertThat(fileListTabFiles).hasSize(3)
                assertThat(fileListTabFiles).isEqualTo(fileListNfsTabFiles(subFolder))
            }

            assertThat(Paths.get("$subFolder/Files/$testFile")).exists()
            assertThat(Paths.get("$subFolder/Files/FileList.xml")).exists()
            assertThat(Paths.get("$subFolder/Files/FileList.json")).exists()
            assertThat(Paths.get("$subFolder/Files/FileList.pagetab.tsv")).exists()

            assertThat(Paths.get("$subFolder/${createdSub.accNo}.xml")).exists()
            assertThat(Paths.get("$subFolder/${createdSub.accNo}.json")).exists()
            assertThat(Paths.get("$subFolder/${createdSub.accNo}.pagetab.tsv")).exists()
        }

        private fun `assertFireSubFiles`(submission: ExtSubmission, accNo: String, subFolder: String) {
            val submissionTabFiles = submission.pageTabFiles as List<FireFile>
            assertThat(submissionTabFiles).hasSize(3)

            val jsonTabFile = submissionTabFiles.first()
            val jsonFile = File("$subFolder/$accNo.json")
            assertThat(jsonTabFile.filePath).isEqualTo("$accNo.json")
            assertThat(jsonTabFile.relPath).isEqualTo("$accNo.json")
            assertThat(jsonTabFile.fireId).isNotNull()
            assertThat(jsonTabFile.md5).isEqualTo(jsonFile.md5())
            assertThat(jsonTabFile.size).isEqualTo(jsonFile.size())

            val xmlTabFile = submissionTabFiles.second()
            val xmlFile = File("$subFolder/$accNo.xml")
            assertThat(xmlTabFile.filePath).isEqualTo("$accNo.xml")
            assertThat(xmlTabFile.relPath).isEqualTo("$accNo.xml")
            assertThat(xmlTabFile.fireId).isNotNull()
            assertThat(xmlTabFile.md5).isEqualTo(xmlFile.md5())
            assertThat(xmlTabFile.size).isEqualTo(xmlFile.size())

            val tsvTabFile = submissionTabFiles.third()
            val tsvFile = File("$subFolder/$accNo.pagetab.tsv")
            assertThat(tsvTabFile.filePath).isEqualTo("$accNo.pagetab.tsv")
            assertThat(tsvTabFile.relPath).isEqualTo("$accNo.pagetab.tsv")
            assertThat(tsvTabFile.fireId).isNotNull()
            assertThat(tsvTabFile.md5).isEqualTo(tsvFile.md5())
            assertThat(tsvTabFile.size).isEqualTo(tsvFile.size())
        }

        private fun `assertFireFileListFiles`(submission: ExtSubmission, accNo: String, subFolder: String) {
            val fileListTabFiles = submission.section.fileList!!.pageTabFiles as List<FireFile>
            assertThat(fileListTabFiles).hasSize(3)

            val jsonTabFile = fileListTabFiles.first()
            val jsonFile = File("$subFolder/Files/FileList.json")
            assertThat(jsonTabFile.filePath).isEqualTo("FileList.json")
            assertThat(jsonTabFile.relPath).isEqualTo("Files/FileList.json")
            assertThat(jsonTabFile.fireId).isNotNull()
            assertThat(jsonTabFile.md5).isEqualTo(jsonFile.md5())
            assertThat(jsonTabFile.size).isEqualTo(jsonFile.size())

            val xmlTabFile = fileListTabFiles.second()
            val xmlFile = File("$subFolder/Files/FileList.xml")
            assertThat(xmlTabFile.filePath).isEqualTo("FileList.xml")
            assertThat(xmlTabFile.relPath).isEqualTo("Files/FileList.xml")
            assertThat(xmlTabFile.fireId).isNotNull()
            assertThat(xmlTabFile.md5).isEqualTo(xmlFile.md5())
            assertThat(xmlTabFile.size).isEqualTo(xmlFile.size())

            val tsvTabFile = fileListTabFiles.third()
            val tsvFile = File("$subFolder/Files/FileList.pagetab.tsv")
            assertThat(tsvTabFile.filePath).isEqualTo("FileList.pagetab.tsv")
            assertThat(tsvTabFile.relPath).isEqualTo("Files/FileList.pagetab.tsv")
            assertThat(tsvTabFile.fireId).isNotNull()
            assertThat(tsvTabFile.md5).isEqualTo(tsvFile.md5())
            assertThat(tsvTabFile.size).isEqualTo(tsvFile.size())
        }

        private fun submissionNfsTabFiles(accNo: String, subFolder: String): List<NfsFile> {
            val jsonPath = "$subFolder/$accNo.json"
            val xmlPath = "$subFolder/$accNo.xml"
            val tsvPath = "$subFolder/$accNo.pagetab.tsv"
            return listOf(
                createNfsFile("$accNo.json", "$accNo.json", File(jsonPath)),
                createNfsFile("$accNo.xml", "$accNo.xml", File(xmlPath)),
                createNfsFile("$accNo.pagetab.tsv", "$accNo.pagetab.tsv", File(tsvPath))
            )
        }

        private fun fileListNfsTabFiles(subFolder: String): List<NfsFile> {
            val jsonName = "FileList.json"
            val xmlName = "FileList.xml"
            val tsvName = "FileList.pagetab.tsv"
            val jsonFile = File(subFolder).resolve("Files/$jsonName")
            val xmlFile = File(subFolder).resolve("Files/$xmlName")
            val tsvFile = File(subFolder).resolve("Files/$tsvName")
            return listOf(
                createNfsFile(jsonName, "Files/$jsonName", jsonFile),
                createNfsFile(xmlName, "Files/$xmlName", xmlFile),
                createNfsFile(tsvName, "Files/$tsvName", tsvFile)
            )
        }
    }
}
