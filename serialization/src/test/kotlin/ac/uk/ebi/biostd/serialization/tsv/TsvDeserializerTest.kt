package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.common.assertAttributes
import ac.uk.ebi.biostd.test.basicSubmission
import ac.uk.ebi.biostd.test.submissionWithDetailedAttributes
import ac.uk.ebi.biostd.test.submissionWithFiles
import ac.uk.ebi.biostd.test.submissionWithFilesTable
import ac.uk.ebi.biostd.test.submissionWithInnerSubsections
import ac.uk.ebi.biostd.test.submissionWithLinks
import ac.uk.ebi.biostd.test.submissionWithLinksTable
import ac.uk.ebi.biostd.test.submissionWithRootSection
import ac.uk.ebi.biostd.test.submissionWithSectionsTable
import ac.uk.ebi.biostd.test.submissionWithSubsection
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.getLeft
import ebi.ac.uk.util.getRight
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
                EMPTY,
                "Study",
                Attribute("Title", "Test Root Section"),
                Attribute("Abstract", "Test abstract"))
    }

    @Test
    fun deserializeSubmissionWithSectionsTable() {
        val submission: Submission = deserializer.deserialize(submissionWithSectionsTable().toString())
        assertThat(submission.rootSection.sections).hasSize(1)

        val sectionsTable: SectionsTable = submission.rootSection.sections.first().getRight()
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

    @Test
    fun deserializeSubsection() {
        val submission: Submission = deserializer.deserialize(submissionWithSubsection().toString())
        assertThat(submission.rootSection.sections).hasSize(1)

        val subSection: Section = submission.rootSection.sections.first().getLeft()
        assertSection(
                subSection,
                "F-001",
                "Funding",
                Attribute("Agency", "National Support Program of China"),
                Attribute("Grant Id", "No. 2015BAD27B01"))
    }

    @Test
    fun deserializeInnerSubsections() {
        val submission: Submission = deserializer.deserialize(submissionWithInnerSubsections().toString())
        assertThat(submission.rootSection.sections).hasSize(2)

        val section1: Section = submission.rootSection.sections.first().getLeft()
        assertThat(section1.sections).hasSize(1)
        assertSection(
                section1,
                "F-001",
                "Funding",
                Attribute("Agency", "National Support Program of China"),
                Attribute("Grant Id", "No. 2015BAD27B01"))
        assertSection(
                section1.sections.first().getLeft(),
                "E-001",
                "Expense",
                Attribute("Description", "Travel"))

        val section2: Section = submission.rootSection.sections.second().getLeft()
        assertThat(section2.sections).isEmpty()
        assertSection(
                section2,
                "F-002",
                "Funding",
                Attribute("Agency", "National Support Program of Japan"),
                Attribute("Grant Id", "No. 2015BAD27A03"))
    }

    @Test
    fun deserializeLinks() {
        val submission: Submission = deserializer.deserialize(submissionWithLinks().toString())

        assertThat(submission.rootSection.links).hasSize(2)
        assertLink(submission.rootSection.links.first().getLeft(), "http://arandomsite.org")
        assertLink(submission.rootSection.links.second().getLeft(), "http://completelyunrelatedsite.org")
    }

    @Test
    fun deserializeLinksTable() {
        val submission: Submission = deserializer.deserialize(submissionWithLinksTable().toString())
        assertThat(submission.rootSection.links).hasSize(1)

        val linksTable: LinksTable = submission.rootSection.links.first().getRight()
        assertThat(linksTable.elements).hasSize(2)
        assertLink(linksTable.elements.first(), "AF069309", Attribute("Type", "gen"))
        assertLink(linksTable.elements.second(), "AF069123", Attribute("Type", "gen"))
    }

    @Test
    fun deserializeFiles() {
        val submission: Submission = deserializer.deserialize(submissionWithFiles().toString())

        assertThat(submission.rootSection.files).hasSize(2)
        assertFile(submission.rootSection.files.first().getLeft(), "12870_2017_1225_MOESM10_ESM.docx")
        assertFile(submission.rootSection.files.second().getLeft(), "12870_2017_1225_MOESM1_ESM.docx")
    }

    @Test
    fun deserializeFilesTable() {
        val submission: Submission = deserializer.deserialize(submissionWithFilesTable().toString())
        assertThat(submission.rootSection.files).hasSize(1)

        val filesTable: FilesTable = submission.rootSection.files.first().getRight()
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

    private fun assertSubmission(
            submission: Submission, accNo: String, title: String, vararg attributes: Attribute) {
        assertThat(submission.accNo).isEqualTo(accNo)
        assertThat(submission.title).isEqualTo(title)
        assertAttributes(submission.attributes, attributes)
    }

    private fun assertSection(
            section: Section, expectedAccNo: String, expectedType: String, vararg expectedAttributes: Attribute) {
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
