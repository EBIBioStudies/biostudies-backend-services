package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.common.getLeft
import ac.uk.ebi.biostd.common.getRight
import ac.uk.ebi.biostd.test.basicSubmission
import ac.uk.ebi.biostd.test.submissionWithDetailedAttributes
import ac.uk.ebi.biostd.test.submissionWithFiles
import ac.uk.ebi.biostd.test.submissionWithFilesTable
import ac.uk.ebi.biostd.test.submissionWithLinks
import ac.uk.ebi.biostd.test.submissionWithLinksTable
import ac.uk.ebi.biostd.test.submissionWithRootSection
import ac.uk.ebi.biostd.test.submissionWithSectionsTable
import ac.uk.ebi.biostd.test.submissionWithSubsection
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
import ebi.ac.uk.model.extensions.type
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
                "Study",
                Attribute("Title", "Test Root Section"),
                Attribute("Abstract", "Test abstract"))
    }

    @Test
    fun deserializeSubmissionWithSectionsTable() {
        val submission: Submission = deserializer.deserialize(submissionWithSectionsTable().toString())
        assertThat(submission.rootSection.sections).hasSize(1)

        val sectionsTable: SectionsTable = submission.rootSection.sections[0].getRight()
        assertThat(sectionsTable.elements).hasSize(2)
        assertSection(sectionsTable.elements[0], "Data", Attribute("Title", "Data 1"), Attribute("Desc", "Group 1"))
        assertSection(sectionsTable.elements[1], "Data", Attribute("Title", "Data 2"), Attribute("Desc", "Group 2"))
    }

    @Test
    fun deserializeSubsection() {
        val submission: Submission = deserializer.deserialize(submissionWithSubsection().toString())
        assertThat(submission.rootSection.sections).hasSize(1)

        val subSection: Section = submission.rootSection.sections[0].getLeft()
        assertSection(
                subSection,
                "Funding",
                Attribute("Agency", "National Support Program of China"),
                Attribute("Grant Id", "No. 2015BAD27B01"))
    }

    @Test
    fun deserializeLinks() {
        val submission: Submission = deserializer.deserialize(submissionWithLinks().toString())

        assertThat(submission.rootSection.links).hasSize(2)
        assertLink(submission.rootSection.links[0].getLeft(), "http://arandomsite.org")
        assertLink(submission.rootSection.links[1].getLeft(), "http://completelyunrelatedsite.org")
    }

    @Test
    fun deserializeLinksTable() {
        val submission: Submission = deserializer.deserialize(submissionWithLinksTable().toString())
        assertThat(submission.rootSection.links).hasSize(1)

        val linksTable: LinksTable = submission.rootSection.links[0].getRight()
        assertThat(linksTable.elements).hasSize(2)
        assertLink(linksTable.elements[0], "AF069309", Attribute("Type", "gen"))
        assertLink(linksTable.elements[1], "AF069123", Attribute("Type", "gen"))
    }

    @Test
    fun deserializeFiles() {
        val submission: Submission = deserializer.deserialize(submissionWithFiles().toString())

        assertThat(submission.rootSection.files).hasSize(2)
        assertFile(submission.rootSection.files[0].getLeft(), "12870_2017_1225_MOESM10_ESM.docx")
        assertFile(submission.rootSection.files[1].getLeft(), "12870_2017_1225_MOESM1_ESM.docx")
    }

    @Test
    fun deserializeFilesTable() {
        val submission: Submission = deserializer.deserialize(submissionWithFilesTable().toString())
        assertThat(submission.rootSection.files).hasSize(1)

        val filesTable: FilesTable = submission.rootSection.files[0].getRight()
        assertThat(filesTable.elements).hasSize(2)
        assertFile(
                filesTable.elements[0],
                "Abstract.pdf",
                Attribute("Description", "An abstract file"),
                Attribute("Usage", "Testing"))
        assertFile(
                filesTable.elements[1],
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

    private fun assertSection(section: Section, expectedType: String, vararg expectedAttributes: Attribute) {
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

    private fun assertAttributes(attributes: List<Attribute>, expectedAttributes: Array<out Attribute>) {
        expectedAttributes.forEachIndexed { index, expectedAttribute ->
            assertAttribute(attributes[index], expectedAttribute)
        }
    }

    private fun assertAttribute(attribute: Attribute, expectedAttribute: Attribute) {
        assertThat(attribute.name).isEqualTo(expectedAttribute.name)
        assertThat(attribute.value).isEqualTo(expectedAttribute.value)
        assertThat(attribute.reference).isEqualTo(expectedAttribute.reference)
        assertAttributeDetails(attribute.nameAttrs, expectedAttribute.nameAttrs)
        assertAttributeDetails(attribute.valueAttrs, expectedAttribute.valueAttrs)
    }

    private fun assertAttributeDetails(
            detailedAttributes: MutableList<AttributeDetail>, expectedDetailedAttributes: MutableList<AttributeDetail>) {
        expectedDetailedAttributes.forEachIndexed { index, expectedDetailedAttribute ->
            assertThat(detailedAttributes[index].name).isEqualTo(expectedDetailedAttribute.name)
            assertThat(detailedAttributes[index].value).isEqualTo(expectedDetailedAttribute.value)
        }
    }
}
