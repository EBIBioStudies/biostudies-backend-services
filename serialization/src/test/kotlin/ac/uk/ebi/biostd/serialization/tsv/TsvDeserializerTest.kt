package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.common.getLeft
import ac.uk.ebi.biostd.common.getRight
import ac.uk.ebi.biostd.tsv.Tsv
import ac.uk.ebi.biostd.tsv.line
import ac.uk.ebi.biostd.tsv.tsv
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constans.FileFields
import ebi.ac.uk.model.constans.LinkFields
import ebi.ac.uk.model.constans.SectionFields
import ebi.ac.uk.model.constans.SubFields
import ebi.ac.uk.model.extensions.Section
import ebi.ac.uk.model.extensions.accNo
import ebi.ac.uk.model.extensions.title
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TsvDeserializerTest {
    private val accNo = "S-EPMC123"
    private val title = "A Submission For Testing"
    private val sectionType = "Study"
    private val sectionAbstract = "Abstract"
    private val sectionAbstractValue = "This is a testing submission designed to test the PageTab deserializer"
    private val pageTab: Tsv = tsv {
        line(SubFields.SUBMISSION.value, accNo)
        line(SubFields.TITLE.value, title)
        line()

        line(sectionType)
        line(SubFields.TITLE.value, title)
        line(sectionAbstract, sectionAbstractValue)
        line()
    }
    private val deserializer = TsvDeserializer()

    @Test
    fun deserializeBaseSubmission() {
        val dataSource = "DataSource"
        val attachTo = "AttachTo"
        val europePMC = "EuropePMC"

        val pageTab: String = tsv {
            line(SubFields.SUBMISSION.value, accNo)
            line(SubFields.TITLE.value, title)
            line(dataSource, europePMC)
            line(attachTo, europePMC)
            line()
        }.toString()

        val submission: Submission = deserializer.deserialize(pageTab)
        assertSubmission(
                submission, accNo, title, Attribute(dataSource, europePMC), Attribute(attachTo, europePMC))
    }

    @Test
    fun deserializeSubmissionWithRootSection() {
        val submission: Submission = deserializer.deserialize(pageTab.toString())
        assertSubmission(submission, accNo, title)
        assertSection(
                submission.rootSection,
                sectionType,
                Attribute(SubFields.TITLE.value, title),
                Attribute(sectionAbstract, sectionAbstractValue))
    }

    @Test
    fun deserializeSubsection() {
        val type = "Funding"
        val attr1 = "Agency"
        val attr2 = "Grant Id"
        val attrVal1 = "National Support Program of China"
        val attrVal2 = "No. 2015BAD27B01"

        pageTab.line(type)
        pageTab.line(attr1, attrVal1)
        pageTab.line(attr2, attrVal2)
        pageTab.line()

        val submission: Submission = deserializer.deserialize(pageTab.toString())
        assertThat(submission.rootSection.sections).hasSize(1)

        val subSection: Section = submission.rootSection.sections[0].getLeft()
        assertSection(subSection, type, Attribute(attr1, attrVal1), Attribute(attr2, attrVal2))
    }

    @Test
    fun deserializeLinks() {
        val url1 = "http://arandomsite.org"
        val url2 = "http://completelyunrelatedsite.org"

        pageTab.line(LinkFields.LINK.value, url1)
        pageTab.line()
        pageTab.line(LinkFields.LINK.value, url2)
        pageTab.line()

        val submission: Submission = deserializer.deserialize(pageTab.toString())
        assertThat(submission.rootSection.links).hasSize(2)
        assertLink(submission.rootSection.links[0].getLeft(), url1)
        assertLink(submission.rootSection.links[1].getLeft(), url2)
    }

    @Test
    fun deserializeLinksTable() {
        val type = "Type"
        val genType = "gen"
        val gen1 = "AF069309"
        val gen2 = "AF069123"

        pageTab.line(SectionFields.LINKS.value, type)
        pageTab.line(gen1, genType)
        pageTab.line(gen2, genType)
        pageTab.line()

        val submission: Submission = deserializer.deserialize(pageTab.toString())
        assertThat(submission.rootSection.links).hasSize(1)

        val linksTable: LinksTable = submission.rootSection.links[0].getRight()
        assertThat(linksTable.elements).hasSize(2)
        assertLink(linksTable.elements[0], gen1, Attribute(type, genType))
        assertLink(linksTable.elements[1], gen2, Attribute(type, genType))
    }

    @Test
    fun deserializeFiles() {
        val file1 = "12870_2017_1225_MOESM10_ESM.docx"
        val file2 = "12870_2017_1225_MOESM1_ESM.docx"

        pageTab.line(FileFields.FILE.value, file1)
        pageTab.line()
        pageTab.line(FileFields.FILE.value, file2)
        pageTab.line()

        val submission: Submission = deserializer.deserialize(pageTab.toString())
        assertThat(submission.rootSection.files).hasSize(2)
        assertFile(submission.rootSection.files[0].getLeft(), file1)
        assertFile(submission.rootSection.files[1].getLeft(), file2)
    }

    @Test
    fun deserializeFilesTable() {
        val desc = "Description"
        val usage = "Usage"

        val file1 = "Abstract.pdf"
        val desc1 = "A test abstract"
        val usage1 = "Testing"

        val file2 = "SuperImportantFile1.docx"
        val desc2 = "A super important file"
        val usage2 = "Important stuff"

        pageTab.line(SectionFields.FILES.value, desc, usage)
        pageTab.line(file1, desc1, usage1)
        pageTab.line(file2, desc2, usage2)
        pageTab.line()

        val submission: Submission = deserializer.deserialize(pageTab.toString())
        assertThat(submission.rootSection.files).hasSize(1)

        val filesTable: FilesTable = submission.rootSection.files[0].getRight()
        assertThat(filesTable.elements).hasSize(2)
        assertFile(filesTable.elements[0], file1, Attribute(desc, desc1), Attribute(usage, usage1))
        assertFile(filesTable.elements[1], file2, Attribute(desc, desc2), Attribute(usage, usage2))
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
        expectedAttributes.forEachIndexed { index, attribute ->
            assertAttribute(attributes[index], attribute.name, attribute.value)
        }
    }

    private fun assertAttribute(attribute: Attribute, expectedName: String, expectedValue: String) {
        assertThat(attribute.name).isEqualTo(expectedName)
        assertThat(attribute.value).isEqualTo(expectedValue)
    }
}
