package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.common.assertAttributes
import ac.uk.ebi.biostd.test.basicSubmission
import ac.uk.ebi.biostd.test.basicSubmissionWithComments
import ac.uk.ebi.biostd.test.submissionWithDetailedAttributes
import ac.uk.ebi.biostd.test.submissionWithFiles
import ac.uk.ebi.biostd.test.submissionWithFilesTable
import ac.uk.ebi.biostd.test.submissionWithInnerSubsections
import ac.uk.ebi.biostd.test.submissionWithInnerSubsectionsTable
import ac.uk.ebi.biostd.test.submissionWithInvalidAttribute
import ac.uk.ebi.biostd.test.submissionWithInvalidInnerSubsection
import ac.uk.ebi.biostd.test.submissionWithInvalidNameAttributeDetail
import ac.uk.ebi.biostd.test.submissionWithInvalidValueAttributeDetail
import ac.uk.ebi.biostd.test.submissionWithLinks
import ac.uk.ebi.biostd.test.submissionWithLinksTable
import ac.uk.ebi.biostd.test.submissionWithRootSection
import ac.uk.ebi.biostd.test.submissionWithSectionsTable
import ac.uk.ebi.biostd.test.submissionWithSubsection
import ac.uk.ebi.biostd.test.submissionWithTableWithMoreAttributes
import ac.uk.ebi.biostd.test.submissionWithTableWithNoRows
import ac.uk.ebi.biostd.tsv.deserialization.TsvDeserializer
import ac.uk.ebi.biostd.validation.INVALID_TABLE_ROW
import ac.uk.ebi.biostd.validation.InvalidChunkSizeException
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.MISPLACED_ATTR_NAME
import ac.uk.ebi.biostd.validation.MISPLACED_ATTR_VAL
import ac.uk.ebi.biostd.validation.REQUIRED_ATTR_VALUE
import ac.uk.ebi.biostd.validation.REQUIRED_TABLE_ROWS
import ac.uk.ebi.biostd.validation.SerializationException
import arrow.core.Either
import ebi.ac.uk.asserts.assertSingleElement
import ebi.ac.uk.asserts.assertSubmission
import ebi.ac.uk.asserts.assertTable
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.util.collections.ifLeft
import ebi.ac.uk.util.collections.ifRight
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

// TODO: refactor this to use group or organize somehow!
class TsvDeserializerTest {
    private val deserializer = TsvDeserializer()

    @Test
    fun `basic submission`() {
        val submission: Submission = deserializer.deserialize(basicSubmission().toString())

        assertSubmission(
            submission,
            "S-EPMC123",
            Attribute("Title", "Basic Submission"),
            Attribute("DataSource", "EuropePMC"),
            Attribute("AttachTo", "EuropePMC"))
    }

    @Test
    fun `basic submission with comments`() {
        val submission: Submission = deserializer.deserialize(basicSubmissionWithComments().toString())

        assertSubmission(
            submission,
            "S-EPMC123",
            Attribute("Title", "Basic Submission"),
            Attribute("DataSource", "EuropePMC"),
            Attribute("AttachTo", "EuropePMC"))
    }

    @Test
    fun `detailed attributes`() {
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
            Attribute("Title", "Submission With Detailed Attributes"),
            detailedAttribute,
            Attribute("affiliation", "EuropePMC", true))
    }

    @Test
    fun `submission with root section`() {
        val submission: Submission = deserializer.deserialize(submissionWithRootSection().toString())

        assertSubmission(submission, "S-EPMC125", Attribute("Title", "Test Submission"))
        assertThat(submission.section).isEqualTo(Section(
            type = "Study",
            attributes = listOf(Attribute("Title", "Test Root Section"), Attribute("Abstract", "Test abstract"))
        ))
    }

    @Test
    fun `submission with sections table`() {
        val submission: Submission = deserializer.deserialize(submissionWithSectionsTable().toString())

        assertThat(submission.section.sections).hasSize(1)
        assertTable(
            submission.section.sections.first(),
            Section(
                accNo = "DT-1",
                type = "Data",
                attributes = listOf(Attribute("Title", "Data 1"), Attribute("Desc", "Group 1"))),
            Section(
                accNo = "DT-2",
                type = "Data",
                attributes = listOf(Attribute("Title", "Data 2"), Attribute("Desc", "Group 2"))))
    }

    @Test
    fun subsection() {
        val submission: Submission = deserializer.deserialize(submissionWithSubsection().toString())

        assertThat(submission.section.sections).hasSize(1)
        assertSingleElement(
            submission.section.sections.first(),
            Section(
                accNo = "F-001",
                type = "Funding",
                attributes = listOf(
                    Attribute("Agency", "National Support Program of China"),
                    Attribute("Grant Id", "No. 2015BAD27B01"))))
    }

    @Test
    fun `inner subsections`() {
        val submission: Submission = deserializer.deserialize(submissionWithInnerSubsections().toString())

        assertThat(submission.section.sections).hasSize(2)
        submission.section.sections.first().ifLeft { section ->
            assertThat(section.sections).hasSize(1)
            assertThat(section).isEqualTo(Section(
                accNo = "F-001",
                type = "Funding",
                attributes = listOf(
                    Attribute("Agency", "National Support Program of China"),
                    Attribute("Grant Id", "No. 2015BAD27B01")),
                sections = mutableListOf(
                    Either.left(Section(
                        type = "Expense",
                        accNo = "E-001",
                        parentAccNo = "F-001",
                        attributes = listOf(Attribute("Description", "Travel"))))
                )))
        }

        submission.section.sections.second().ifLeft { section ->
            assertThat(section.sections).isEmpty()
            assertThat(section).isEqualTo(Section(
                accNo = "F-002",
                type = "Funding",
                attributes = listOf(
                    Attribute("Agency", "National Support Program of Japan"),
                    Attribute("Grant Id", "No. 2015BAD27A03"))))
        }
    }

    @Test
    fun `subsection with invalid parent`() {
        assertThrows<SerializationException> {
            deserializer.deserialize(submissionWithInvalidInnerSubsection().toString())
        }
    }

    @Test
    fun `inner subsections table`() {
        val submission: Submission = deserializer.deserialize(submissionWithInnerSubsectionsTable().toString())
        assertThat(submission.section.sections).hasSize(2)

        submission.section.sections.first().ifLeft { section ->
            assertThat(section.sections).isEmpty()
            assertThat(section).isEqualTo(Section(
                accNo = "F-001",
                type = "Funding",
                attributes = listOf(
                    Attribute("Agency", "National Support Program of China"),
                    Attribute("Grant Id", "No. 2015BAD27B01"))))
        }

        submission.section.sections.second().ifLeft { section ->
            assertThat(section.sections).hasSize(1)

            assertThat(section).isEqualTo(
                Section(
                    accNo = "S-001",
                    type = "Study",
                    attributes = listOf(Attribute("Type", "Imaging")),
                    sections = mutableListOf(
                        Either.right(SectionsTable(listOf(
                            Section(
                                accNo = "SMP-1",
                                type = "Sample",
                                parentAccNo = "S-001",
                                attributes = listOf(Attribute("Title", "Sample1"), Attribute("Desc", "Measure 1"))),
                            Section(
                                accNo = "SMP-2",
                                type = "Sample",
                                parentAccNo = "S-001",
                                attributes = listOf(Attribute("Title", "Sample2"), Attribute("Desc", "Measure 2")))
                        )))
                    )))
        }
    }

    @Test
    fun links() {
        val submission: Submission = deserializer.deserialize(submissionWithLinks().toString())

        assertThat(submission.section.links).hasSize(2)
        assertSingleElement(submission.section.links.first(), Link("http://arandomsite.org"))
        assertSingleElement(submission.section.links.second(), Link("http://completelyunrelatedsite.org"))
    }

    @Test
    fun `links table`() {
        val submission: Submission = deserializer.deserialize(submissionWithLinksTable().toString())

        assertThat(submission.section.links).hasSize(1)
        assertTable(
            submission.section.links.first(),
            Link("AF069309", listOf(Attribute("Type", "gen"))),
            Link("AF069123", listOf(Attribute("Type", "gen"))))
    }

    @Test
    fun files() {
        val submission: Submission = deserializer.deserialize(submissionWithFiles().toString())

        assertThat(submission.section.files).hasSize(2)
        submission.section.files.first().ifLeft { file -> assertFile(file, "12870_2017_1225_MOESM10_ESM.docx") }
        submission.section.files.second().ifLeft { file -> assertFile(file, "12870_2017_1225_MOESM1_ESM.docx") }
    }

    @Test
    fun `files table`() {
        val submission: Submission = deserializer.deserialize(submissionWithFilesTable().toString())
        assertThat(submission.section.files).hasSize(1)

        submission.section.files.first().ifRight { filesTable ->
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

    @Test
    fun `single file`() {
        val tsv = tsv {
            line("File", "File1.txt")
            line("Attr", "Value")
            line()
        }.toString()

        val file = deserializer.deserializeElement(tsv, File::class.java)

        assertThat(file).isEqualTo(File("File1.txt", attributes = listOf(Attribute("Attr", "Value"))))
    }

    @Test
    fun `single files table`() {
        val tsv = tsv {
            line("Files", "Attr")
            line("File1.txt", "Value")
            line()
        }.toString()

        val filesTable = deserializer.deserializeElement(tsv, FilesTable::class.java)

        assertThat(filesTable).isEqualTo(
            FilesTable(listOf(File("File1.txt", attributes = listOf(Attribute("Attr", "Value"))))))
    }

    @Test
    fun `single link`() {
        val tsv = tsv {
            line("Link", "http://alink.org")
            line("Attr", "Value")
            line()
        }.toString()

        val link = deserializer.deserializeElement(tsv, Link::class.java)

        assertThat(link).isEqualTo(Link("http://alink.org", attributes = listOf(Attribute("Attr", "Value"))))
    }

    @Test
    fun `single links table`() {
        val tsv = tsv {
            line("Links", "Attr")
            line("http://alink.org", "Value")
            line()
        }.toString()

        val linksTable = deserializer.deserializeElement(tsv, LinksTable::class.java)

        assertThat(linksTable).isEqualTo(
            LinksTable(listOf(Link("http://alink.org", attributes = listOf(Attribute("Attr", "Value"))))))
    }

    @Test
    fun `table containing a row with less attributes`() {
        val tsv = tsv {
            line("Links", "Attr1", "Attr2")
            line("AF069307", "Value 1", "Value 2")
            line("AF069308", "", "Value 2")
            line("AF069309", "Value 1")
            line()
        }.toString()

        val linksTable = deserializer.deserializeElement(tsv, LinksTable::class.java)

        assertThat(linksTable).isEqualTo(
            LinksTable(listOf(
                Link("AF069307", attributes = listOf(Attribute("Attr1", "Value 1"), Attribute("Attr2", "Value 2"))),
                Link("AF069308", attributes = listOf(Attribute("Attr2", "Value 2"))),
                Link("AF069309", attributes = listOf(Attribute("Attr1", "Value 1"))))))
    }

    @Test
    fun `invalid single element`() {
        val tsv = tsv {
            line("Submission", "S-BIAD2")
            line("Title", "A Title")
            line()
        }.toString()

        assertThrows<NotImplementedError> { deserializer.deserializeElement(tsv, Submission::class.java) }
    }

    @Test
    fun `invalid processing class`() {
        val tsv = tsv {
            line("Links", "Attr")
            line("http://alink.org", "Value")
            line()
        }.toString()

        assertThrows<ClassCastException> { deserializer.deserializeElement(tsv, File::class.java) }
    }

    @Test
    fun `empty single element`() {
        assertThrows<InvalidChunkSizeException> { deserializer.deserializeElement("", File::class.java) }
    }

    @Test
    fun `invalid chunk size`() {
        val tsv = tsv {
            line("Links", "Attr")
            line("http://alink.org", "Value")
            line()

            line("Links", "Attr")
            line("http://otherlink.org", "Value")
            line()
        }.toString()

        assertThrows<InvalidChunkSizeException> { deserializer.deserializeElement(tsv, Link::class.java) }
    }

    @Test
    fun `invalid attribute`() =
        assertInvalidElementException(
            assertThrows { deserializer.deserialize(submissionWithInvalidAttribute().toString()) }, REQUIRED_ATTR_VALUE)

    @Test
    fun `invalid name attribute detail`() =
        assertInvalidElementException(assertThrows {
            deserializer.deserialize(submissionWithInvalidNameAttributeDetail().toString()) }, MISPLACED_ATTR_NAME)

    @Test
    fun `invalid value attribute detail`() =
        assertInvalidElementException(assertThrows {
            deserializer.deserialize(submissionWithInvalidValueAttributeDetail().toString()) }, MISPLACED_ATTR_VAL)

    @Test
    fun `table with no rows`() =
        assertInvalidElementException(assertThrows {
            deserializer.deserialize(submissionWithTableWithNoRows().toString()) }, REQUIRED_TABLE_ROWS)

    @Test
    fun `table with more attributes than expected`() =
        assertInvalidElementException(
            assertThrows { deserializer.deserialize(submissionWithTableWithMoreAttributes().toString()) },
            String.format(INVALID_TABLE_ROW, 1, 2))

    private fun assertInvalidElementException(exception: SerializationException, expectedMessage: String) {
        assertThat(exception.errors.values()).hasSize(1)

        val cause = exception.errors.values().first().cause
        assertThat(cause).isInstanceOf(InvalidElementException::class.java)
        assertThat(cause).hasMessage("$expectedMessage. Element was not created.")
    }

    private fun assertFile(file: File, expectedName: String, vararg expectedAttributes: Attribute) {
        assertThat(file.path).isEqualTo(expectedName)
        assertAttributes(file.attributes, expectedAttributes)
    }
}
