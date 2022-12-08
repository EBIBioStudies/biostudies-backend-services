package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.FilePersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import arrow.core.Either
import ebi.ac.uk.asserts.assertThat
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
import ebi.ac.uk.io.ext.createOrReplaceFile
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.collections.ifRight
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpecialSubmissionAttributesTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired val tagsRefRepository: TagDataRepository,
    @Autowired val toSubmissionMapper: ToSubmissionMapper,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(SuperUser)
        webClient = getWebClient(serverPort, SuperUser)

        tagsRefRepository.save(DbTag(classifier = "classifier", name = "tag"))
    }

    @Test
    fun `15-1 new submission with past release date`() {
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
    fun `15-2 submission with tags`() {
        val submission = tsv {
            line("Submission", "S-TEST123")
            line("Title", "Submission With Tags")
            line()

            line("Study", "SECT-001", "", "classifier:tag")
            line()
        }.toString()

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
        val submitted = toSubmissionMapper.toSimpleSubmission(submissionRepository.getExtByAccNo("S-TEST123"))
        assertThat(submitted).isEqualTo(
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
    fun `15-3 new submission with empty accNo subsection table`() {
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
    fun `15-4 new submission with empty-null attributes`() {
        fun assertSubmission(submission: ExtSubmission) {
            assertThat(submission.accNo).isEqualTo("S-STBL124")
            assertThat(submission.title).isEqualTo("Test Section Table")

            val submissionAttributes = submission.attributes
            assertThat(submissionAttributes).hasSize(2)
            assertThat(submissionAttributes.first())
                .isEqualTo(ExtAttribute("Submission Empty Attribute", null))
            assertThat(submissionAttributes.second())
                .isEqualTo(ExtAttribute("Submission Null Attribute", null))
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
        webClient.uploadFile(ITestListener.tempFolder.createOrReplaceFile(fileName))

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
    fun `15-5 new submission with empty-null table attributes`() {
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
        webClient.uploadFile(ITestListener.tempFolder.createOrReplaceFile(fileName))

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
}
