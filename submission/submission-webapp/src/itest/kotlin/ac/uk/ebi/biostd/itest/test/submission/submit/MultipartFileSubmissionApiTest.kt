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
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.excel.excel
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import java.io.File
import java.nio.file.Paths
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

// TODO Fix all integration tests
@ExtendWith(TemporaryFolderExtension::class)
internal class MultipartFileSubmissionApiTest(
    private val tempFolder: TemporaryFolder,
) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Transactional
    @DirtiesContext
    inner class SingleSubmissionTest(
        @Autowired private val submissionRepository: SubmissionQueryService,
        @Autowired private val securityTestService: SecurityTestService,
        @Autowired private val toSubmissionMapper: ToSubmissionMapper
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
            val excelPageTab = excel(File("${tempFolder.root.absolutePath}/ExcelSubmission.xlsx")) {
                sheet("page tab") {
                    row {
                        cell("Submission")
                        cell("S-EXC123")
                    }
                    row {
                        cell("Title")
                        cell("Excel Submission")
                    }

                    row { cell(""); cell("") }
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

            val fileList = excel(File("${tempFolder.root.absolutePath}/FileList.xlsx")) {
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

            val response =
                webClient.submitSingle(submission, JSON, listOf(fileList, tempFolder.createFile("File2.txt")))
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

            val savedSubmission = getSimpleSubmission("S-TEST6")
            assertThat(savedSubmission.attributes).hasSize(3)
            assertThat(savedSubmission["Exp"]).isEqualTo("1")
            assertThat(savedSubmission["Type"]).isEqualTo("Exp")
            assertThat(savedSubmission["Title"]).isEqualTo("Test Submission")
        }

        @Test
        fun `invalid format file`() {
            val submission = tempFolder.createFile("submission.txt", "invalid file")

            assertThatExceptionOfType(WebClientException::class.java)
                .isThrownBy { webClient.submitSingle(submission, emptyList()) }
                .withMessageContaining("Unsupported page tab format submission.txt")
        }

        private fun getSimpleSubmission(accNo: String) =
            toSubmissionMapper.toSimpleSubmission(submissionRepository.getExtByAccNo(accNo))

        private fun assertSubmissionFiles(accNo: String, testFile: String) {
            val createdSub = submissionRepository.getExtByAccNo(accNo)
            val subFolder = "$submissionPath/${createdSub.relPath}"

            if (mongoMode)
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

            assertThat(Paths.get("$subFolder/Files/$testFile")).exists()
            assertThat(Paths.get("$subFolder/Files/FileList.xml")).exists()
            assertThat(Paths.get("$subFolder/Files/FileList.json")).exists()
            assertThat(Paths.get("$subFolder/Files/FileList.pagetab.tsv")).exists()

            assertThat(Paths.get("$subFolder/${createdSub.accNo}.xml")).exists()
            assertThat(Paths.get("$subFolder/${createdSub.accNo}.json")).exists()
            assertThat(Paths.get("$subFolder/${createdSub.accNo}.pagetab.tsv")).exists()
        }

        private fun submissionFireTabFiles(accNo: String, subFolder: String): List<FireFile> {
            val jsonName = "$accNo.json"
            val xmlName = "$accNo.xml"
            val tsvName = "$accNo.pagetab.tsv"
            val jsonFile = File("$subFolder/$accNo.json")
            val xmlFile = File("$subFolder/$accNo.xml")
            val tsvFile = File("$subFolder/$accNo.pagetab.tsv")
            return listOf(
                FireFile(jsonName, jsonName, "fireOid-$jsonName", jsonFile.md5(), jsonFile.size(), listOf()),
                FireFile(xmlName, xmlName, "fireOid-$xmlName", xmlFile.md5(), xmlFile.size(), listOf()),
                FireFile(tsvName, tsvName, "fireOid-$tsvName", tsvFile.md5(), tsvFile.size(), listOf())
            )
        }

        private fun fileListFireTabFiles(subFolder: String): List<FireFile> {
            val jsonName = "FileList.json"
            val xmlName = "FileList.xml"
            val tsvName = "FileList.pagetab.tsv"
            val json = File("$subFolder/Files/$jsonName")
            val xml = File("$subFolder/Files/FileList.xml")
            val tsv = File("$subFolder/Files/FileList.pagetab.tsv")
            return listOf(
                FireFile(jsonName, "Files/$jsonName", "fireOid-$jsonName", json.md5(), json.size(), listOf()),
                FireFile(xmlName, "Files/$xmlName", "fireOid-$xmlName", xml.md5(), xml.size(), listOf()),
                FireFile(tsvName, "Files/$tsvName", "fireOid-$tsvName", tsv.md5(), tsv.size(), listOf())
            )
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
