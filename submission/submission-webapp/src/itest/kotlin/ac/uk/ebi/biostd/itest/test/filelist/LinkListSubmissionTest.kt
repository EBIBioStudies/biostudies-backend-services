package ac.uk.ebi.biostd.itest.test.filelist

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.enableFire
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.util.collections.ifRight
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
import java.io.File
import java.nio.file.Paths

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LinkListSubmissionTest(
    @LocalServerPort val serverPort: Int,
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init(): Unit =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

    @Test
    fun `31-1 TSV submission with link list`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-LLT311")
                    line("Title", "TSV Submission With Link List")
                    line()

                    line("Study", "SECT-001")
                    line("Link List", "LinkList.tsv")
                    line()
                }.toString()

            val linksList =
                tempFolder.createFile(
                    "LinkList.tsv",
                    tsv {
                        line("Links", "Type")
                        line("IHECRE00000919.1", "EpiRR")
                        line()
                    }.toString(),
                )

            val parameters = SubmitParameters(storageMode = storageMode)
            val files = listOf(linksList)
            assertThat(webClient.submitMultipart(submission, TSV, parameters, files)).isSuccessful()

            val savedSubmission = submissionRepository.getExtByAccNo("S-LLT311")
            val links = savedSubmission.section.links
            assertThat(links).hasSize(1)
            links.first().ifRight {
                assertThat(it.links).hasSize(1)
                assertThat(it.links.first().url).isEqualTo("IHECRE00000919.1")
            }

            assertSubmissionFiles(accNo = "S-LLT311", linkListName = "LinkList")
        }

    @Test
    fun `31-2 JSON submission with link list`() =
        runTest {
            val submission =
                jsonObj {
                    "accno" to "S-LLT312"
                    "attributes" to
                        jsonArray({
                            "name" to "Title"
                            "value" to "JSON Submission With Link List"
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
                                    "name" to "Link List"
                                    "value" to "LinkList.json"
                                },
                            )
                    }
                }.toString()

            val linksList =
                tempFolder.createFile(
                    "LinkList.json",
                    jsonArray({
                        "url" to "IHECRE00000919.1"
                        "attributes" to
                            jsonArray(
                                {
                                    "name" to "Type"
                                    "value" to "EpiRR"
                                },
                            )
                    }).toString(),
                )

            val parameters = SubmitParameters(storageMode = storageMode)
            val files = listOf(linksList)
            assertThat(webClient.submitMultipart(submission, JSON, parameters, files)).isSuccessful()

            val savedSubmission = submissionRepository.getExtByAccNo("S-LLT312")
            val links = savedSubmission.section.links
            assertThat(links).hasSize(1)
            links.first().ifRight {
                assertThat(it.links).hasSize(1)
                assertThat(it.links.first().url).isEqualTo("IHECRE00000919.1")
            }

            assertSubmissionFiles(accNo = "S-LLT312", linkListName = "LinkList")
        }

    private suspend fun assertSubmissionFiles(
        accNo: String,
        linkListName: String,
    ) {
        val createdSub = submissionRepository.getExtByAccNo(accNo)
        val subFolder = "$submissionPath/${createdSub.relPath}"

        if (enableFire) {
            assertFireSubFiles(createdSub, accNo, subFolder)
            assertFireLinkListFiles(createdSub, linkListName, subFolder)
        } else {
            val submissionTabFiles = createdSub.pageTabFiles
            assertThat(submissionTabFiles).hasSize(2)
            assertThat(submissionTabFiles).isEqualTo(submissionNfsTabFiles(accNo, subFolder))

            val fileListTabFiles = createdSub.section.linkList!!.pageTabFiles
            assertThat(fileListTabFiles).hasSize(2)
            assertThat(fileListTabFiles).isEqualTo(linkListNfsTabFiles(linkListName, subFolder))
        }

        assertThat(Paths.get("$subFolder/Files/$linkListName.json")).exists()
        assertThat(Paths.get("$subFolder/Files/$linkListName.tsv")).exists()

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

    private fun assertFireLinkListFiles(
        sub: ExtSubmission,
        linkListName: String,
        subFolder: String,
    ) {
        val linkListTabFiles = sub.section.linkList!!.pageTabFiles
        assertThat(linkListTabFiles).hasSize(2)

        val jsonTabFile = linkListTabFiles.first() as FireFile
        val jsonFile = File("$subFolder/Files/$linkListName.json")
        assertThat(jsonTabFile.filePath).isEqualTo("$linkListName.json")
        assertThat(jsonTabFile.relPath).isEqualTo("Files/$linkListName.json")
        assertThat(jsonTabFile.fireId).isNotNull()
        assertThat(jsonTabFile.md5).isEqualTo(jsonFile.md5())
        assertThat(jsonTabFile.size).isEqualTo(jsonFile.size())

        val tsvTabFile = linkListTabFiles.second() as FireFile
        val tsvFile = File("$subFolder/Files/$linkListName.tsv")
        assertThat(tsvTabFile.filePath).isEqualTo("$linkListName.tsv")
        assertThat(tsvTabFile.relPath).isEqualTo("Files/$linkListName.tsv")
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

    private fun linkListNfsTabFiles(
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
