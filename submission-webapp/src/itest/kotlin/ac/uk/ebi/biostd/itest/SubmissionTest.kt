package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.config.PersistenceConfig
import ac.uk.ebi.biostd.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.itest.common.setAppProperty
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.invalidLinkUrl
import ac.uk.ebi.biostd.itest.factory.simpleSubmissionTsv
import ac.uk.ebi.biostd.persistence.service.ExtSubmissionRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import arrow.core.Either
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.attributeDetails
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.security.integration.model.SignUpRequest
import ebi.ac.uk.security.service.SecurityService
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertThrows
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
import org.springframework.web.client.HttpClientErrorException

const val BASE_PATH_PLACEHOLDER = "{BASE_PATH}"

@ExtendWith(TemporaryFolderExtension::class)
@TestInstance(PER_CLASS)
class SubmissionTest(private val tempFolder: TemporaryFolder) {
    private lateinit var basePath: String

    @BeforeAll
    fun init() {
        basePath = tempFolder.root.absolutePath
        setAppProperty(BASE_PATH_PLACEHOLDER, basePath)
    }

    @AfterAll
    fun tearDown() {
        setAppProperty(basePath, BASE_PATH_PLACEHOLDER)
    }

    @Nested
    @TestInstance(PER_CLASS)
    @ExtendWith(SpringExtension::class)
    @Import(value = [SubmitterConfig::class, PersistenceConfig::class, FileConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    inner class SimpleSubmission {

        @LocalServerPort
        private var serverPort: Int = 0

        @Autowired
        private lateinit var submissionRepository: SubmissionRepository

        @Autowired
        private lateinit var extSubmissionRepository: ExtSubmissionRepository

        @Autowired
        private lateinit var securityService: SecurityService

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            securityService.registerUser(SignUpRequest("test@biostudies.com", "jhon_doe", "12345"))
            webClient = BioWebClient.create("http://localhost:$serverPort", securityService.login("jhon_doe", "12345"))
            webClient.uploadFiles(listOf(tempFolder.createFile("LibraryFile1.txt"), tempFolder.createFile("LibraryFile2.txt")))
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
            assertSavedSubmission("S-EPMC124")
        }

        @Test
        fun `submit all in one Json submission`() {
            val response = webClient.submitSingle(allInOneSubmissionJson().toString(), SubmissionFormat.JSON)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertSavedSubmission("S-EPMC125")
        }

        @Test
        fun `resubmit existing submission`() {
            val accNo = "S-ABC123"
            val title = "Simple Submission"
            val submission = simpleSubmissionTsv().toString()
            val response = webClient.submitSingle(submission, SubmissionFormat.TSV)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertExtSubmission(accNo, title)

            val resubmitResponse = webClient.submitSingle(submission, SubmissionFormat.TSV)
            assertThat(resubmitResponse).isNotNull
            assertThat(resubmitResponse.statusCode).isEqualTo(HttpStatus.OK)
            assertExtSubmission(accNo, title, 2)
        }

        @Test
        fun `submit with invalid link Url`() {
            val exception = assertThrows(HttpClientErrorException::class.java) { webClient.submitSingle(invalidLinkUrl().toString(), SubmissionFormat.TSV) }

            assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }

        private fun assertExtSubmission(accNo: String, expectedTitle: String, expectedVersion: Int = 1) {
            val submission = extSubmissionRepository.findByAccNo(accNo)

            assertThat(submission.title).isEqualTo(expectedTitle)
            assertThat(submission.version).isEqualTo(expectedVersion)
        }

        private fun assertSavedSubmission(accNo: String) {
            val submission = submissionRepository.findByAccNo(accNo)
            assertThat(submission).hasAccNo(accNo)
            assertThat(submission).hasExactly(Attribute("Title", "venous blood, Monocyte"))

            val rootSection = submission.section
            assertSections(rootSection)
            assertLinks(rootSection)
            assertFiles(rootSection)
        }

        private fun assertSections(rootSection: Section) {
            assertThat(rootSection).has("SECT-001", "Study")
            assertThat(rootSection.attributes).containsExactly(
                Attribute("Project", "CEEHRC (McGill)"),
                Attribute("Organization", "Org1", true),
                Attribute(
                    "Tissue type",
                    "venous blood",
                    false,
                    attributeDetails("Tissue", "Blood"),
                    attributeDetails("Ontology", "UBERON")))

            assertThat(rootSection.sections).hasSize(2)
            assertFirstSection(rootSection.sections[0])
            assertSecondSection(rootSection.sections[1])
        }

        private fun assertSecondSection(sectionEither: Either<Section, SectionsTable>) {
            val sectionTable = assertThat(sectionEither).isTable()
            assertThat(sectionTable.elements).hasSize(1)

            val sectionElement = sectionTable.elements[0]
            assertThat(sectionElement).has("DT-1", "Data")
            assertThat(sectionElement.attributes).containsExactly(
                Attribute("Title", "Group 1 Transcription Data"),
                Attribute("Description", "The data for zygotic transcription in mammals group 1"))
        }

        private fun assertFirstSection(sectionEither: Either<Section, SectionsTable>) {
            val section = assertThat(sectionEither).isSection()
            assertThat(section).has("SUBSECT-001", "Stranded Total RNA-Seq")

            val linksTable = assertThat(section.links[0]).isTable()
            assertThat(linksTable.elements).hasSize(1)

            val tableLink = linksTable.elements[0]
            assertThat(tableLink.url).isEqualTo("EGAD00001001282")
            assertThat(tableLink.attributes).containsExactly(
                Attribute("Type", "EGA"),
                Attribute("Assay type", "RNA-Seq"))
        }

        private fun assertLinks(rootSection: Section) {
            assertThat(rootSection.links).hasSize(1)
            val link = assertThat(rootSection.links[0]).isLink()
            assertThat(link).isEqualTo(Link("AF069309", listOf(Attribute("Type", "gen"))))
        }

        private fun assertFiles(section: Section) {
            assertThat(section.files).hasSize(2)

            val file = assertThat(section.files[0]).isFile()
            assertThat(file.name).isEqualTo("LibraryFile1.txt")
            assertThat(file.attributes).containsExactly(Attribute("Description", "Library File 1"))

            val fileTable = assertThat(section.files[1]).isTable()
            assertThat(fileTable.elements).hasSize(1)

            val tableFile = fileTable.elements[0]
            assertThat(tableFile.name).isEqualTo("LibraryFile2.txt")
            assertThat(tableFile.attributes).hasSize(2)
            assertThat(tableFile.attributes[0]).isEqualTo(Attribute("Description", "Library File 2"))
            assertThat(tableFile.attributes[1]).isEqualTo(Attribute("Type", "Lib"))
        }
    }
}
