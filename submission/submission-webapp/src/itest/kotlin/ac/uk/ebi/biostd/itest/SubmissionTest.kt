package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.GenericUser
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionXml
import ac.uk.ebi.biostd.itest.factory.invalidLinkUrl
import ac.uk.ebi.biostd.itest.factory.simpleSubmissionTsv
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import arrow.core.Either
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.attributeDetails
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.HttpClientErrorException
import java.nio.file.Paths

/**
 * Integration test for submission in all formats using "all features includes" submission example.
 */
@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [SubmitterConfig::class, PersistenceConfig::class, FileConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SingleSubmissionTest(@Autowired val submissionRepository: SubmissionRepository) {

        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(RegisterRequest(GenericUser.email, GenericUser.username, GenericUser.password))
            webClient = securityClient.getAuthenticatedClient(GenericUser.username, GenericUser.password)

            tempFolder.createDirectory("Folder1")
            tempFolder.createDirectory("Folder1/Folder2")

            webClient.uploadFiles(listOf(tempFolder.createFile("DataFile1.txt"), tempFolder.createFile("DataFile2.txt")))
            webClient.uploadFiles(listOf(tempFolder.createFile("Folder1/DataFile3.txt")), "Folder1")
            webClient.uploadFiles(listOf(tempFolder.createFile("Folder1/Folder2/DataFile4.txt")), "Folder1/Folder2")
        }

        @Test
        fun `submit all in one TSV submission`() {
            val response = webClient.submitSingle(allInOneSubmissionTsv().toString(), SubmissionFormat.TSV)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertSavedSubmission("S-EPMC124")
        }

        @Test
        fun `submit all in one JSON submission`() {
            val response = webClient.submitSingle(allInOneSubmissionJson().toString(), SubmissionFormat.JSON)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertSavedSubmission("S-EPMC125")
        }

        @Test
        fun `submit all in one XML submission`() {
            val response = webClient.submitSingle(allInOneSubmissionXml().toString(), SubmissionFormat.XML)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertSavedSubmission("S-EPMC126")
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
            val exception = assertThrows(HttpClientErrorException::class.java) {
                webClient.submitSingle(invalidLinkUrl().toString(), SubmissionFormat.TSV)
            }

            assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }

        private fun assertExtSubmission(accNo: String, expectedTitle: String, expectedVersion: Int = 1) {
            val submission = submissionRepository.getExtendedByAccNo(accNo)

            assertThat(submission.title).isEqualTo(expectedTitle)
            assertThat(submission.version).isEqualTo(expectedVersion)
        }

        private fun assertSavedSubmission(accNo: String) {
            val submission = submissionRepository.getExtendedByAccNo(accNo)
            assertThat(submission).hasAccNo(accNo)
            assertThat(submission).hasExactly(Attribute("Title", "venous blood, Monocyte"))

            val rootSection = submission.section
            assertSections(rootSection)
            assertLinks(rootSection)
            assertFiles(rootSection, submission.relPath)
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

        private fun assertFiles(section: Section, submissionRelPath: String) {
            assertThat(section.files).hasSize(2)
            val submissionFolderPath = "$basePath/submission/$submissionRelPath/Files"

            val file = assertThat(section.files.first()).isFile()
            assertFile(file, submissionFolderPath, "DataFile1.txt", Attribute("Description", "Data File 1"))

            val fileTable = assertThat(section.files.second()).isTable()
            assertThat(fileTable.elements).hasSize(3)

            assertFile(
                fileTable.elements.first(),
                submissionFolderPath,
                "DataFile2.txt",
                Attribute("Description", "Data File 2"),
                Attribute("Type", "Data"))

            assertFile(
                fileTable.elements.second(),
                submissionFolderPath,
                "Folder1/DataFile3.txt",
                Attribute("Description", "Data File 3"),
                Attribute("Type", "Data"))

            assertFile(
                fileTable.elements.third(),
                submissionFolderPath,
                "Folder1/Folder2/DataFile4.txt",
                Attribute("Description", "Data File 4"),
                Attribute("Type", "Data"))
        }

        private fun assertFile(
            file: File,
            submissionFolderPath: String,
            expectedPath: String,
            vararg expectedAttributes: Attribute
        ) {
            assertThat(file.path).isEqualTo(expectedPath)

            assertThat(file.attributes).hasSize(expectedAttributes.size)
            expectedAttributes.forEachIndexed { index, attribute ->
                assertThat(file.attributes[index]).isEqualTo(attribute)
            }

            assertThat(Paths.get("$submissionFolderPath/$expectedPath")).exists()
        }
    }
}
