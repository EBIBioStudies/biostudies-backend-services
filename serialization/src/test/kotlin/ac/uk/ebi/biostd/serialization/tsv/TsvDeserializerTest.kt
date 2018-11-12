package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.common.assertAttributes
import ac.uk.ebi.biostd.test.basicSubmission
import ac.uk.ebi.biostd.test.submissionWithDetailedAttributes
import ac.uk.ebi.biostd.test.submissionWithFiles
import ac.uk.ebi.biostd.test.submissionWithFilesTable
import ac.uk.ebi.biostd.test.submissionWithInnerSubsections
import ac.uk.ebi.biostd.test.submissionWithInnerSubsectionsTable
import ac.uk.ebi.biostd.test.submissionWithLinks
import ac.uk.ebi.biostd.test.submissionWithLinksTable
import ac.uk.ebi.biostd.test.submissionWithRootSection
import ac.uk.ebi.biostd.test.submissionWithSectionsTable
import ac.uk.ebi.biostd.test.submissionWithSubsection
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.File
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.collections.ifLeft
import ebi.ac.uk.util.collections.ifRight
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TsvDeserializerTest {

    private val deserializer = TsvDeserializer()

    @Test
    fun deserializeBaseSubmission() {
        val submission: Submission = deserializer.deserialize(basicSubmission().toString())

        assertSubmission(
                submission,
                "S-EPMC123",
                "Basic Submission",
                Attribute("DataSource", "EuropePMC"),
                Attribute("AttachTo", "EuropePMC"))
    }

    @Test
    fun deserializeDetailedAttributes() {
        val submission: Submission = deserializer.deserialize(submissionWithDetailedAttributes().toString())
        val detailedAttribute = Attribute(
                "Submission Type",
                "RNA-seq of non coding RNA",
                false,
                mutableListOf(AttributeDetail("Seq Type", "RNA")),
                mutableListOf(AttributeDetail("Ontology", "EFO")))

        assertSubmission(
                submission,
                "S-EPMC124",
                "Submission With Detailed Attributes",
                detailedAttribute,
                Attribute("affiliation", "EuropePMC", true))
    }

    @Test
    fun deserializeSubmissionWithRootSection() {
        val submission: Submission = deserializer.deserialize(submissionWithRootSection().toString())
        assertSubmission(submission, "S-EPMC125", "Test Submission")
        assertSection(
                submission.rootSection,
                null,
                "Study",
                Attribute("Title", "Test Root Section"),
                Attribute("Abstract", "Test abstract"))
    }

    @Test
    fun deserializeSubmissionWithSectionsTable() {
        val submission: Submission = deserializer.deserialize(submissionWithSectionsTable().toString())
        assertThat(submission.rootSection.sections).hasSize(1)

        submission.rootSection.sections.first().ifRight { sectionsTable ->
            assertThat(sectionsTable.elements).hasSize(2)
            assertSection(
                    sectionsTable.elements.first(),
                    "DT-1",
                    "Data",
                    Attribute("Title", "Data 1"), Attribute("Desc", "Group 1"))
            assertSection(
                    sectionsTable.elements.second(),
                    "DT-2",
                    "Data",
                    Attribute("Title", "Data 2"), Attribute("Desc", "Group 2"))
        }
    }

    @Test
    fun deserializeSubsection() {
        val submission: Submission = deserializer.deserialize(submissionWithSubsection().toString())
        assertThat(submission.rootSection.sections).hasSize(1)
        submission.rootSection.sections.first().ifLeft { subsection ->
            assertSection(
                    subsection,
                    "F-001",
                    "Funding",
                    Attribute("Agency", "National Support Program of China"),
                    Attribute("Grant Id", "No. 2015BAD27B01"))
        }
    }

    @Test
    fun deserializeInnerSubsections() {
        val submission: Submission = deserializer.deserialize(submissionWithInnerSubsections().toString())
        assertThat(submission.rootSection.sections).hasSize(2)

        submission.rootSection.sections.first().ifLeft { section ->
            assertThat(section.sections).hasSize(1)
            assertSection(
                    section,
                    "F-001",
                    "Funding",
                    Attribute("Agency", "National Support Program of China"),
                    Attribute("Grant Id", "No. 2015BAD27B01"))

            section.sections.first().ifLeft { subsection ->
                assertSection(subsection, "E-001", "Expense", Attribute("Description", "Travel"))
            }
        }

        submission.rootSection.sections.second().ifLeft { section ->
            assertThat(section.sections).isEmpty()
            assertSection(
                    section,
                    "F-002",
                    "Funding",
                    Attribute("Agency", "National Support Program of Japan"),
                    Attribute("Grant Id", "No. 2015BAD27A03"))
        }
    }

    @Test
    fun deserializeInnerSubsectionsTable() {
        val submission: Submission = deserializer.deserialize(submissionWithInnerSubsectionsTable().toString())
        assertThat(submission.rootSection.sections).hasSize(2)

        submission.rootSection.sections.first().ifLeft { section ->
            assertThat(section.sections).isEmpty()
            assertSection(
                    section,
                    "F-001",
                    "Funding",
                    Attribute("Agency", "National Support Program of China"),
                    Attribute("Grant Id", "No. 2015BAD27B01"))
        }

        submission.rootSection.sections.second().ifLeft { section ->
            assertThat(section.sections).hasSize(1)
            assertSection(
                    section,
                    "S-001",
                    "Study",
                    Attribute("Type", "Imaging"))

            section.sections.first().ifRight { sectionsTable ->
                assertThat(sectionsTable.elements).hasSize(2)
                assertSection(
                        sectionsTable.elements.first(),
                        "SMP-1",
                        "Sample",
                        Attribute("Title", "Sample1"), Attribute("Desc", "Measure 1"))
                assertSection(
                        sectionsTable.elements.second(),
                        "SMP-2",
                        "Sample",
                        Attribute("Title", "Sample2"), Attribute("Desc", "Measure 2"))
            }
        }
    }

    @Test
    fun deserializeLinks() {
        val submission: Submission = deserializer.deserialize(submissionWithLinks().toString())

        assertThat(submission.rootSection.links).hasSize(2)
        submission.rootSection.links.first().ifLeft { link -> assertLink(link, "http://arandomsite.org") }
        submission.rootSection.links.second().ifLeft { link -> assertLink(link, "http://completelyunrelatedsite.org") }
    }

    @Test
    fun deserializeLinksTable() {
        val submission: Submission = deserializer.deserialize(submissionWithLinksTable().toString())
        assertThat(submission.rootSection.links).hasSize(1)

        submission.rootSection.links.first().ifRight { linksTable ->
            assertThat(linksTable.elements).hasSize(2)
            assertLink(linksTable.elements.first(), "AF069309", Attribute("Type", "gen"))
            assertLink(linksTable.elements.second(), "AF069123", Attribute("Type", "gen"))
        }
    }

    @Test
    fun deserializeFiles() {
        val submission: Submission = deserializer.deserialize(submissionWithFiles().toString())

        assertThat(submission.rootSection.files).hasSize(2)
        submission.rootSection.files.first().ifLeft { file -> assertFile(file, "12870_2017_1225_MOESM10_ESM.docx") }
        submission.rootSection.files.second().ifLeft { file -> assertFile(file, "12870_2017_1225_MOESM1_ESM.docx") }
    }

    @Test
    fun deserializeFilesTable() {
        val submission: Submission = deserializer.deserialize(submissionWithFilesTable().toString())
        assertThat(submission.rootSection.files).hasSize(1)

        submission.rootSection.files.first().ifRight { filesTable ->
            assertThat(filesTable.elements).hasSize(2)
            assertFile(
                    filesTable.elements.first(),
                    "Abstract.pdf",
                    Attribute("Description", "An abstract file"),
                    Attribute("Usage", "Testing"))
            assertFile(
                    filesTable.elements.second(),
                    "SuperImportantFile1.docx",
                    Attribute("Description", "A super important file"),
                    Attribute("Usage", "Important stuff"))
        }
    }

    private fun assertSubmission(
        submission: Submission,
        accNo: String,
        title: String,
        vararg attributes: Attribute
    ) {
        assertThat(submission.accNo).isEqualTo(accNo)
        assertThat(submission.title).isEqualTo(title)
        assertAttributes(submission.attributes, attributes)
    }

    private fun assertSection(
        section: Section,
        expectedAccNo: String?,
        expectedType: String,
        vararg expectedAttributes: Attribute
    ) {
        assertThat(section.accNo).isEqualTo(expectedAccNo)
        assertThat(section.type).isEqualTo(expectedType)
        assertAttributes(section.attributes, expectedAttributes)
    }

    private fun assertLink(link: Link, expectedUrl: String, vararg expectedAttributes: Attribute) {
        assertThat(link.url).isEqualTo(expectedUrl)
        assertAttributes(link.attributes, expectedAttributes)
    }

    private fun assertFile(file: File, expectedName: String, vararg expectedAttributes: Attribute) {
        assertThat(file.name).isEqualTo(expectedName)
        assertAttributes(file.attributes, expectedAttributes)
    }
}
