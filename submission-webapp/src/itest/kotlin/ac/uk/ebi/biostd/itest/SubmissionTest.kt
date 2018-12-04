package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.config.PersistenceConfig
import ac.uk.ebi.biostd.config.SubmitterConfig
import ac.uk.ebi.biostd.itest.common.setAppProperty
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionTsv
import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.asserts.assertSingleElement
import ebi.ac.uk.asserts.assertSubmission
import ebi.ac.uk.asserts.assertTable
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.File
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constans.SubFields
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.paths.FolderResolver
import ebi.ac.uk.security.integration.model.SignUpRequest
import ebi.ac.uk.security.service.SecurityService
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

const val BASE_PATH_PLACEHOLDER = "{BASE_PATH}"

@ExtendWith(TemporaryFolderExtension::class)
@TestInstance(PER_CLASS)
class SubmissionTest(private val temporaryFolder: TemporaryFolder) {
    private lateinit var basePath: String

    @BeforeAll
    fun init() {
        basePath = temporaryFolder.root.absolutePath
        setAppProperty(BASE_PATH_PLACEHOLDER, basePath)
    }

    @AfterAll
    fun tearDown() {
        setAppProperty(basePath, BASE_PATH_PLACEHOLDER)
    }

    @Nested
    @TestInstance(PER_CLASS)
    @ExtendWith(SpringExtension::class)
    @Import(value = [SubmitterConfig::class, PersistenceConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    inner class SimpleSubmission {

        @LocalServerPort
        private var randomServerPort: Int = 0

        @Autowired
        private lateinit var submissionRepository: SubmissionRepository

        @Autowired
        private lateinit var securityService: SecurityService

        @Autowired
        private lateinit var folderResolver: FolderResolver

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            val user = securityService.registerUser(SignUpRequest("test@biostudies.com", "jhon_doe", "12345"))
            webClient = BioWebClient.create(
                baseUrl = "http://localhost:$randomServerPort",
                token = securityService.login("jhon_doe", "12345"))
            setUpMockUserFiles(user)
        }

        @Test
        fun `submit simple submission`() {
            val accNo = "SimpleAcc1"
            val title = "Simple Submission"
            val submission = Submission(accNo = accNo)
            submission[SubFields.TITLE] = title

            val response = webClient.submitSingle(submission, SubmissionFormat.XML)

            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

            val savedSubmission = submissionRepository.findByAccNo(accNo)
            assertThat(savedSubmission).isNotNull
            assertThat(savedSubmission.accNo).isEqualTo(accNo)
            assertThat(savedSubmission.title).isEqualTo(title)
        }

        @Test
        fun `submit all in one TSV submission`() {
            val response = webClient.submitSingle(allInOneSubmissionTsv().toString(), SubmissionFormat.TSV)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertSavedSubmission()
        }

        private fun assertSavedSubmission() {
            val submission = submissionRepository.findByAccNo("S-EPMC124")
            assertSubmission(submission, "S-EPMC124", "venous blood, Monocyte")

            val rootSection = submission.section
            assertSections(rootSection)
            assertLinks(rootSection)
            assertFiles(rootSection)
        }

        private fun assertSections(rootSection: Section) {
            assertThat(rootSection.sections).hasSize(2)
            assertThat(rootSection).isEqualTo(Section(
                type = "Study",
                accNo = "SECT-001",
                attributes = listOf(
                    Attribute("Project", "CEEHRC (McGill)"),
                    Attribute("Organization", "Org1", true),
                    Attribute(
                        "Tissue type",
                        "venous blood",
                        false,
                        mutableListOf(AttributeDetail("Tissue", "Blood")),
                        mutableListOf(AttributeDetail("Ontology", "UBERON"))))))

            assertThat(rootSection.sections).hasSize(2)
            assertSingleElement(
                rootSection.sections.first(), Section(accNo = "SUBSECT-001", type = "Stranded Total RNA-Seq"))
            assertTable(
                rootSection.sections.second(),
                Section(
                    type = "Data",
                    accNo = "DT-1",
                    attributes = listOf(
                        Attribute("Title", "Group 1 Transcription Data"),
                        Attribute("Description", "The data for zygotic transcription in mammals group 1"))))
        }

        private fun assertLinks(rootSection: Section) {
            assertThat(rootSection.links).hasSize(2)
            assertSingleElement(rootSection.links.first(), Link("AF069309", listOf(Attribute("Type", "gen"))))
            assertTable(
                rootSection.links.second(),
                Link("EGAD00001001282", listOf(Attribute("Type", "EGA"), Attribute("Assay type", "RNA-Seq"))))
        }

        private fun assertFiles(rootSection: Section) {
            assertThat(rootSection.files).hasSize(2)
            assertSingleElement(
                rootSection.files.first(), File("LibraryFile1.txt", listOf(Attribute("Description", "Library File 1"))))
            assertTable(
                rootSection.files.second(),
                File("LibraryFile2.txt", listOf(Attribute("Description", "Library File 2"), Attribute("Type", "Lib"))))
        }

        private fun setUpMockUserFiles(user: User) {
            val userFolder =
                folderResolver.getUserMagicFolderPath(user.id, user.secret).toString().substringAfter(".tmp/")

            temporaryFolder.createDirectory(userFolder.substringBefore("/"))
            temporaryFolder.createDirectory(userFolder)

            temporaryFolder.createFile("$userFolder/LibraryFile1.txt")
            temporaryFolder.createFile("$userFolder/LibraryFile2.txt")
        }
    }
}
