package ac.uk.ebi.biostd.tsv

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
import ac.uk.ebi.biostd.tsv.deserialization.TsvDeserializer
import ebi.ac.uk.asserts.assertSingleElement
import ebi.ac.uk.asserts.assertSubmission
import ebi.ac.uk.asserts.assertTable
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.File
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
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
            Attribute("Title", "Basic Submission"),
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
            Attribute("Title", "Submission With Detailed Attributes"),
            detailedAttribute,
            Attribute("affiliation", "EuropePMC", true))
    }

    @Test
    fun deserializeSubmissionWithRootSection() {
        val submission: Submission = deserializer.deserialize(submissionWithRootSection().toString())

        assertSubmission(submission, "S-EPMC125", Attribute("Title", "Test Submission"))
        assertThat(submission.section).isEqualTo(Section(
            type = "Study",
            attributes = listOf(Attribute("Title", "Test Root Section"), Attribute("Abstract", "Test abstract"))
        ))
    }

    @Test
    fun deserializeSubmissionWithSectionsTable() {
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
    fun deserializeSubsection() {
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
    fun deserializeInnerSubsections() {
        val submission: Submission = deserializer.deserialize(submissionWithInnerSubsections().toString())

        assertThat(submission.section.sections).hasSize(2)
        submission.section.sections.first().ifLeft { section ->
            assertThat(section.sections).hasSize(1)

            assertThat(section).isEqualTo(Section(
                accNo = "F-001",
                type = "Funding",
                attributes = listOf(
                    Attribute("Agency", "National Support Program of China"),
                    Attribute("Grant Id", "No. 2015BAD27B01"))))

            assertSingleElement(
                section.sections.first(),
                Section(accNo = "E-001", type = "Expense", attributes = listOf(Attribute("Description", "Travel"))))
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
    fun deserializeInnerSubsectionsTable() {
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
                Section(accNo = "S-001", type = "Study", attributes = listOf(Attribute("Type", "Imaging"))))

            assertTable(section.sections.first(),
                Section(
                    accNo = "SMP-1",
                    type = "Sample",
                    attributes = listOf(Attribute("Title", "Sample1"), Attribute("Desc", "Measure 1"))),
                Section(
                    accNo = "SMP-2",
                    type = "Sample",
                    attributes = listOf(Attribute("Title", "Sample2"), Attribute("Desc", "Measure 2"))))
        }
    }

    @Test
    fun deserializeLinks() {
        val submission: Submission = deserializer.deserialize(submissionWithLinks().toString())

        assertThat(submission.section.links).hasSize(2)
        assertSingleElement(submission.section.links.first(), Link("http://arandomsite.org"))
        assertSingleElement(submission.section.links.second(), Link("http://completelyunrelatedsite.org"))
    }

    @Test
    fun deserializeLinksTable() {
        val submission: Submission = deserializer.deserialize(submissionWithLinksTable().toString())

        assertThat(submission.section.links).hasSize(1)
        assertTable(
            submission.section.links.first(),
            Link("AF069309", listOf(Attribute("Type", "gen"))),
            Link("AF069123", listOf(Attribute("Type", "gen"))))
    }

    @Test
    fun deserializeFiles() {
        val submission: Submission = deserializer.deserialize(submissionWithFiles().toString())

        assertThat(submission.section.files).hasSize(2)
        submission.section.files.first().ifLeft { file -> assertFile(file, "12870_2017_1225_MOESM10_ESM.docx") }
        submission.section.files.second().ifLeft { file -> assertFile(file, "12870_2017_1225_MOESM1_ESM.docx") }
    }

    @Test
    fun deserializeFilesTable() {
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

    private fun assertFile(file: File, expectedName: String, vararg expectedAttributes: Attribute) {
        assertThat(file.name).isEqualTo(expectedName)
        assertAttributes(file.attributes, expectedAttributes)
    }
}
