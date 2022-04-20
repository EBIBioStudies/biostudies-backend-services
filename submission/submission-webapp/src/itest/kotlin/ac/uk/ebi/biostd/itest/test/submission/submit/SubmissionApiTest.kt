package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.common.clean
import ac.uk.ebi.biostd.itest.common.createUser
import ac.uk.ebi.biostd.itest.common.getWebClient
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.factory.invalidLinkUrl
import ac.uk.ebi.biostd.itest.listener.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.listener.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.model.DbSequence
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import arrow.core.Either
import ebi.ac.uk.api.dto.UserRegistration
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.collections.ifRight
import ebi.ac.uk.util.collections.second
import java.io.File
import kotlin.test.assertFailsWith
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

@Import(PersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionApiTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionQueryService,
    @Autowired val sequenceRepository: SequenceDataRepository,
    @Autowired val tagsRefRepository: TagDataRepository,
    @Autowired val userDataRepository: UserDataRepository,
    @Autowired val toSubmissionMapper: ToSubmissionMapper,
    @LocalServerPort val serverPort: Int
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        tempFolder.clean()
        sequenceRepository.deleteAll()
        tagsRefRepository.deleteAll()
        securityTestService.deleteSuperUser()

        securityTestService.registerUser(SuperUser)
        webClient = getWebClient(serverPort, SuperUser)

        sequenceRepository.save(DbSequence("S-BSST"))
        tagsRefRepository.save(DbTag(classifier = "classifier", name = "tag"))
    }

    @BeforeEach
    fun beforeEach() {
        tempFolder.clean()
    }

    @Test
    fun `submit with submission object`() {
        val submission = submission("SimpleAcc1") {
            title = "Simple Submission"
        }

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
        assertThat(getSimpleSubmission("SimpleAcc1")).isEqualTo(
            submission("SimpleAcc1") {
                title = "Simple Submission"
            }
        )
    }

    @Test
    fun `empty accNo`() {
        val submission = tsv {
            line("Submission")
            line("Title", "Empty AccNo")
        }.toString()

        val response = webClient.submitSingle(submission, TSV)

        assertThat(response).isSuccessful()
        assertThat(getSimpleSubmission(response.body.accNo)).isEqualTo(
            submission(response.body.accNo) {
                title = "Empty AccNo"
            }
        )
    }

    @Test
    fun `submission with root path`() {
        val submission = tsv {
            line("Submission", "S-12364")
            line("Title", "Sample Submission")
            line("RootPath", "RootPathFolder")
            line()

            line("Study")
            line()

            line("File", "DataFile5.txt")
            line()
        }.toString()

        tempFolder.createDirectory("RootPathFolder")
        webClient.uploadFiles(
            listOf(tempFolder.createNewFile("RootPathFolder/DataFile5.txt", "An example content")),
            "RootPathFolder"
        )

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
        assertThat(getSimpleSubmission("S-12364")).isEqualTo(
            submission("S-12364") {
                title = "Sample Submission"
                rootPath = "RootPathFolder"
                section("Study") { file("DataFile5.txt") }
            }
        )
    }

    @Test
    fun `re submit a submission with rootPath`() {
        val rootPath = "The-RootPath"
        val dataFile = "DataFile7.txt"

        val submission = tsv {
            line("Submission", "S-356789")
            line("Title", "Sample Submission")
            line("RootPath", rootPath)
            line()
            line("Study")
            line()
            line("File", "DataFile7.txt")
            line()
        }.toString()

        webClient.uploadFiles(listOf(tempFolder.createNewFile("DataFile7.txt")), rootPath)
        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

        webClient.deleteFile(dataFile, rootPath)

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
    }

    @Test
    fun `submission with on behalf another user`() {
        securityTestService.deleteRegularUser()
        createUser(RegularUser, serverPort)

        val submission = tsv {
            line("Submission")
            line("Title", "Submission Title")
        }.toString()

        val onBehalfClient = SecurityWebClient.create("http://localhost:$serverPort")
            .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

        val response = onBehalfClient.submitSingle(submission, TSV)
        assertThat(response).isSuccessful()

        val accNo = response.body.accNo
        assertThat(getSimpleSubmission(accNo)).isEqualTo(
            submission(accNo) {
                title = "Submission Title"
            }
        )
    }

    @Test
    fun `submission with on behalf new user`() {
        val username = "Jhon doe"
        val email = "jhon@doe.email.com"

        val submission = tsv {
            line("Submission")
            line("Title", "Submission Title")
        }.toString()

        val response = webClient.submitSingle(submission, TSV, UserRegistration(username, email))
        val saved = submissionRepository.getExtByAccNo(response.body.accNo)

        assertThat(saved.owner).isEqualTo(email)
        assertThat(saved.submitter).isEqualTo(SuperUser.email)
        val newUser = userDataRepository.findByEmail(email)
        assertThat(newUser).isNotNull
        assertThat(newUser!!.active).isFalse
        assertThat(newUser.notificationsEnabled).isFalse
    }

    @Test
    fun `submission with generic root section`() {
        val submission = tsv {
            line("Submission", "E-MTAB123")
            line("Title", "Generic Submission")
            line()

            line("Experiment")
            line()
        }.toString()

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
        assertThat(getSimpleSubmission("E-MTAB123")).isEqualTo(
            submission("E-MTAB123") {
                title = "Generic Submission"
                section("Experiment") { }
            }
        )
    }

    @Test
    fun `submission with tags`() {
        val submission = tsv {
            line("Submission", "S-TEST123")
            line("Title", "Submission With Tags")
            line()

            line("Study", "SECT-001", "", "classifier:tag")
            line()
        }.toString()

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
        assertThat(getSimpleSubmission("S-TEST123")).isEqualTo(
            submission("S-TEST123") {
                title = "Submission With Tags"
                section("Study") {
                    accNo = "SECT-001"
                    tags = mutableListOf(Pair("Classifier", "Tag"))
                }
            }
        )
    }

    @Test
    fun `submission with group file`() {
        val groupName = "The-Group"
        val submission = tsv {
            line("Submission", "S-54896")
            line("Title", "Sample Submission")
            line()

            line("Study")
            line()

            line("File", "groups/$groupName/GroupFile1.txt")
            line()

            line("File", "groups/$groupName/folder/GroupFile2.txt")
            line()
        }.toString()

        webClient.addUserInGroup(webClient.createGroup(groupName, "group-desc").name, SuperUser.email)
        webClient.uploadGroupFiles(groupName, listOf(tempFolder.createNewFile("GroupFile1.txt")))
        webClient.uploadGroupFiles(groupName, listOf(tempFolder.createNewFile("GroupFile2.txt")), "folder")

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
        assertThat(getSimpleSubmission("S-54896")).isEqualTo(
            submission("S-54896") {
                title = "Sample Submission"
                section("Study") {
                    file("groups/$groupName/GroupFile1.txt")
                    file("groups/$groupName/folder/GroupFile2.txt")
                }
            }
        )
    }

    @Test
    fun `resubmit existing submission`() {
        fun submission(accNo: String? = null) = tsv {
            if (accNo == null) line("Submission") else line("Submission", accNo)
            line("Title", "Simple Submission With Files 2")
            line("ReleaseDate", "2020-01-25")
            line()

            line("Study")
            line("Type", "Experiment")
            line("File List", "file-list.tsv")
            line()

            line("File", "file section.doc")
            line("Type", "test")
            line()

            line("Experiment", "Exp1")
            line("Type", "Subsection")
            line()

            line("File", "fileSubSection.txt")
            line("Type", "Attached")
            line()
        }.toString()

        val fileListContent = tsv {
            line("Files", "Type")
            line("a/fileFileList.pdf", "inner")
            line("a", "folder")
        }.toString()

        webClient.uploadFiles(
            listOf(
                tempFolder.createNewFile("fileSubSection.txt", "content"),
                tempFolder.createNewFile("file-list.tsv", fileListContent),
                tempFolder.createNewFile("file section.doc"),
            )
        )
        webClient.uploadFiles(listOf(tempFolder.createNewFile("fileFileList.pdf")), "a")

        val response = webClient.submitSingle(submission(), TSV)

        assertThat(response).isSuccessful()
        val accNo = response.body.accNo
        val submitted = submissionRepository.getExtByAccNo(accNo)
        assertThat(submitted.version).isEqualTo(1)
        assertThat(File("$submissionPath/${submitted.relPath}/Files/file section.doc")).exists()
        assertThat(File("$submissionPath/${submitted.relPath}/Files/fileSubSection.txt")).exists()
        assertThat(File("$submissionPath/${submitted.relPath}/Files/fileSubSection.txt")).hasContent("content")
        assertThat(File("$submissionPath/${submitted.relPath}/Files/a/fileFileList.pdf")).exists()

        val changedFile = tempFolder.resolve("fileSubSection.txt").apply { writeText("newContent") }
        webClient.uploadFiles(listOf(changedFile))

        val reSubmitResponse = webClient.submitSingle(submission(accNo), TSV)

        assertThat(reSubmitResponse).isSuccessful()
        val resubmitted = submissionRepository.getExtByAccNo(accNo)
        assertThat(resubmitted.version).isEqualTo(2)
        assertThat(File("$submissionPath/${resubmitted.relPath}/Files/file section.doc")).exists()
        assertThat(File("$submissionPath/${resubmitted.relPath}/Files/fileSubSection.txt")).exists()
        assertThat(File("$submissionPath/${resubmitted.relPath}/Files/fileSubSection.txt")).hasContent("newContent")
        assertThat(File("$submissionPath/${resubmitted.relPath}/Files/a/fileFileList.pdf")).exists()
    }

    @Test
    fun `resubmit existing submission with the same files`() {
        fun submission(accNo: String? = null) = tsv {
            if (accNo == null) line("Submission") else line("Submission", accNo)
            line("Title", "Simple Submission With Files 2")
            line("ReleaseDate", "2020-01-25")
            line()

            line("Study")
            line("Type", "Experiment")
            line("File List", "file-list.tsv")
            line()

            line("File", "file section.doc")
            line("Type", "test")
            line()

            line("Experiment", "Exp1")
            line("Type", "Subsection")
            line()

            line("File", "fileSubSection.txt")
            line("Type", "Attached")
            line()
        }.toString()

        val fileListContent = tsv {
            line("Files", "Type")
            line("a/fileFileList.pdf", "inner")
            line("a", "folder")
        }.toString()

        webClient.uploadFiles(
            listOf(
                tempFolder.createNewFile("fileSubSection.txt", "content"),
                tempFolder.createNewFile("file-list.tsv", fileListContent),
                tempFolder.createNewFile("file section.doc"),
            )
        )
        webClient.uploadFiles(listOf(tempFolder.createNewFile("fileFileList.pdf")), "a")

        val response = webClient.submitSingle(submission(), TSV)
        val accNo = response.body.accNo

        val submitted = submissionRepository.getExtByAccNo(accNo)
        assertThat(response).isSuccessful()
        assertThat(submitted.version).isEqualTo(1)
        assertThat(File("$submissionPath/${submitted.relPath}/Files/file section.doc")).exists()
        assertThat(File("$submissionPath/${submitted.relPath}/Files/fileSubSection.txt")).exists()
        assertThat(File("$submissionPath/${submitted.relPath}/Files/fileSubSection.txt")).hasContent("content")
        assertThat(File("$submissionPath/${submitted.relPath}/Files/a/fileFileList.pdf")).exists()

        val reSubmitResponse = webClient.submitSingle(submission(accNo), TSV)
        assertThat(reSubmitResponse).isSuccessful()
        val resubmitted = submissionRepository.getExtByAccNo(accNo)
        assertThat(resubmitted.version).isEqualTo(2)
        assertThat(File("$submissionPath/${resubmitted.relPath}/Files/file section.doc")).exists()
        assertThat(File("$submissionPath/${resubmitted.relPath}/Files/fileSubSection.txt")).exists()
        assertThat(File("$submissionPath/${resubmitted.relPath}/Files/fileSubSection.txt")).hasContent("content")
        assertThat(File("$submissionPath/${resubmitted.relPath}/Files/a/fileFileList.pdf")).exists()
    }

    @Test
    fun `new submission with past release date`() {
        val submission = tsv {
            line("Submission", "S-RLSD123")
            line("Title", "Test Public Submission")
            line("ReleaseDate", "2000-01-31")
            line()
        }.toString()

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

        val savedSubmission = submissionRepository.getExtByAccNo("S-RLSD123")
        assertThat(savedSubmission.accNo).isEqualTo("S-RLSD123")
        assertThat(savedSubmission.title).isEqualTo("Test Public Submission")
        assertThat(savedSubmission.released).isTrue
    }

    @Test
    fun `new submission with empty accNo subsection table`() {
        val submission = tsv {
            line("Submission", "S-STBL123")
            line("Title", "Test Section Table")
            line()

            line("Study", "SECT-001")
            line()

            line("Data[SECT-001]", "Title")
            line("", "Group 1")
            line()
        }.toString()

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

        val savedSubmission = submissionRepository.getExtByAccNo("S-STBL123")
        assertThat(savedSubmission.accNo).isEqualTo("S-STBL123")
        assertThat(savedSubmission.title).isEqualTo("Test Section Table")

        val section = savedSubmission.section
        assertThat(section.accNo).isEqualTo("SECT-001")
        assertThat(section.sections).hasSize(1)
        section.sections.first().ifRight {
            assertThat(it.sections).hasSize(1)

            val subSection = it.sections.first()
            assertThat(subSection.accNo).isEmpty()
            assertThat(subSection.attributes).hasSize(1)
            assertThat(subSection.attributes.first()).isEqualTo(ExtAttribute("Title", "Group 1"))
        }
    }

    @Test
    fun `new submission with empty-null attributes`() {
        fun assertSubmission(submission: ExtSubmission) {
            assertThat(submission.accNo).isEqualTo("S-STBL124")
            assertThat(submission.title).isEqualTo("Test Section Table")

            val submissionAttributes = submission.attributes
            assertThat(submissionAttributes).hasSize(2)
            assertThat(submissionAttributes.first()).isEqualTo(ExtAttribute("Submission Empty Attribute", null))
            assertThat(submissionAttributes.second()).isEqualTo(ExtAttribute("Submission Null Attribute", null))
        }

        fun assertSection(section: ExtSection) {
            assertThat(section.accNo).isEqualTo("SECT-001")

            val sectionAttributes = section.attributes
            assertThat(sectionAttributes).hasSize(2)
            assertThat(sectionAttributes.first()).isEqualTo(ExtAttribute("Section Empty Attribute", null))
            assertThat(sectionAttributes.second()).isEqualTo(ExtAttribute("Section Null Attribute", null))
        }

        fun assertLinks(links: List<Either<ExtLink, ExtLinkTable>>) {
            assertThat(links).hasSize(1)
            assertThat(links.first()).hasLeftValueSatisfying {
                assertThat(it.url).isEqualTo("www.linkTable.com")

                val attributes = it.attributes
                assertThat(attributes).hasSize(2)
                assertThat(attributes.first()).isEqualTo(ExtAttribute("Link Empty Attribute", null))
                assertThat(attributes.second()).isEqualTo(ExtAttribute("Link Null Attribute", null))
            }
        }

        fun assertFiles(files: List<Either<ExtFile, ExtFileTable>>, fileName: String) {
            assertThat(files).hasSize(1)
            assertThat(files.first()).hasLeftValueSatisfying {
                assertThat(it.filePath).isEqualTo(fileName)
                assertThat(it.relPath).isEqualTo("Files/$fileName")

                val fileAttributes = it.attributes
                assertThat(fileAttributes).hasSize(2)
                assertThat(fileAttributes.first()).isEqualTo(ExtAttribute("File Empty Attribute", null))
                assertThat(fileAttributes.second()).isEqualTo(ExtAttribute("File Null Attribute", null))
            }
        }

        fun assertSubSections(sections: List<Either<ExtSection, ExtSectionTable>>) {
            assertThat(sections).hasSize(1)
            assertThat(sections.first()).hasLeftValueSatisfying {
                val attributes = it.attributes
                assertThat(attributes.first()).isEqualTo(ExtAttribute("SubSection Empty Attribute", null))
                assertThat(attributes.second()).isEqualTo(ExtAttribute("SubSection Null Attribute", null))
            }
        }

        val fileName = "DataFile.txt"
        webClient.uploadFile(tempFolder.createOrReplaceFile(fileName))

        val submission = tsv {
            line("Submission", "S-STBL124")
            line("Title", "Test Section Table")
            line("Submission Empty Attribute", "")
            line("Submission Null Attribute")
            line()

            line("Study", "SECT-001")
            line("Section Empty Attribute", "")
            line("Section Null Attribute")
            line()

            line("Link", "www.linkTable.com")
            line("Link Empty Attribute", "")
            line("Link Null Attribute")
            line()

            line("File", fileName)
            line("File Empty Attribute", "")
            line("File Null Attribute")
            line()

            line("SubSection", "F-001")
            line("SubSection Empty Attribute", "")
            line("SubSection Null Attribute")
            line()
        }.toString()

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

        val savedSubmission = submissionRepository.getExtByAccNo("S-STBL124")

        assertSubmission(savedSubmission)
        assertSection(savedSubmission.section)
        assertLinks(savedSubmission.section.links)
        assertFiles(savedSubmission.section.files, fileName)
        assertSubSections(savedSubmission.section.sections)
    }

    @Test
    fun `new submission with empty-null table attributes`() {
        fun assertSubmission(submission: ExtSubmission) {
            assertThat(submission.accNo).isEqualTo("S-STBL124")
            assertThat(submission.title).isEqualTo("Test Section Table")
            assertThat(submission.section.accNo).isEqualTo("SECT-001")
        }

        fun assertLinks(links: List<Either<ExtLink, ExtLinkTable>>) {
            assertThat(links).hasSize(1)
            assertThat(links.first()).hasRightValueSatisfying { linkTable ->
                val tableLinks = linkTable.links
                assertThat(tableLinks).hasSize(1)

                val tableLink = tableLinks.first()
                assertThat(tableLink.url).isEqualTo("www.linkTable.com")

                val attributes = tableLink.attributes
                assertThat(attributes).hasSize(2)
                assertThat(attributes.first()).isEqualTo(ExtAttribute("Link Empty Attribute", null))
                assertThat(attributes.second()).isEqualTo(ExtAttribute("Link Null Attribute", null))
            }
        }

        fun assertFiles(files: List<Either<ExtFile, ExtFileTable>>, fileName: String) {
            assertThat(files).hasSize(1)
            assertThat(files.first()).hasRightValueSatisfying { fileTable ->
                val tableFile = fileTable.files
                assertThat(tableFile).hasSize(1)

                val file = tableFile.first()
                assertThat(file.filePath).isEqualTo(fileName)
                assertThat(file.relPath).isEqualTo("Files/$fileName")

                val attributes = file.attributes
                assertThat(attributes).hasSize(2)
                assertThat(attributes.first()).isEqualTo(ExtAttribute("File Empty Attribute", null))
                assertThat(attributes.second()).isEqualTo(ExtAttribute("File Null Attribute", null))
            }
        }

        fun assertSubSections(sections: List<Either<ExtSection, ExtSectionTable>>) {
            assertThat(sections).hasSize(1)
            assertThat(sections.first()).hasRightValueSatisfying { sectionTable ->
                val subSections = sectionTable.sections
                assertThat(subSections).hasSize(1)
                val attributes = subSections.first().attributes
                assertThat(attributes.first()).isEqualTo(
                    ExtAttribute(
                        name = "Empty Attr",
                        value = null,
                        nameAttrs = listOf(ExtAttributeDetail("TermId", "EFO_0002768")),
                        valueAttrs = listOf(ExtAttributeDetail("NullValue", null))
                    )
                )
                assertThat(attributes.second()).isEqualTo(
                    ExtAttribute(
                        name = "Null Attr",
                        value = null,
                        nameAttrs = listOf(ExtAttributeDetail("NullName", null)),
                        valueAttrs = listOf(ExtAttributeDetail("Ontology", "EFO"))
                    )
                )
            }
        }

        val fileName = "DataFile.txt"
        webClient.uploadFile(tempFolder.createOrReplaceFile(fileName))

        val submission = tsv {
            line("Submission", "S-STBL124")
            line("Title", "Test Section Table")
            line()

            line("Study", "SECT-001")
            line()

            line("Links", "Link Empty Attribute", "Link Null Attribute")
            line("www.linkTable.com", "")
            line()

            line("Files", "File Empty Attribute", "File Null Attribute")
            line(fileName, "")
            line()

            line("Data[SECT-001]", "Empty Attr", "(TermId)", "[NullValue]", "Null Attr", "(NullName)", "[Ontology]")
            line("DT-1", "", "EFO_0002768", "", "", "", "EFO")
            line()
        }.toString()

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

        val savedSubmission = submissionRepository.getExtByAccNo("S-STBL124")
        assertSubmission(savedSubmission)

        val section = savedSubmission.section
        assertLinks(section.links)
        assertFiles(section.files, fileName)
        assertSubSections(section.sections)
    }

    @Test
    fun `submit with invalid link Url`() {
        val exception = assertThrows(WebClientException::class.java) {
            webClient.submitSingle(invalidLinkUrl().toString(), TSV)
        }

        assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `submission with validation error`() {
        val submission = submission("S-400") {
            title = "Submission with invalid file"
            section("Study") { file("invalidfile.txt") }
        }

        val exception = assertFailsWith<WebClientException> {
            webClient.submitSingle(submission, SubmissionFormat.XML)
        }
        assertThat(exception.message!!.contains("Submission contains invalid files invalid file.txt"))
    }

    private fun getSimpleSubmission(accNo: String) =
        toSubmissionMapper.toSimpleSubmission(submissionRepository.getExtByAccNo(accNo))

    private fun File.createOrReplaceFile(name: String): File {
        if (exists()) delete()
        return createNewFile(name)
    }
}
