package ebi.ac.uk.dsl

import arrow.core.Either
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SubDslTest {
    @Test
    fun `submission with attributes`() {
        val submission = submission("ABC-123") {
            attribute("AttrName", "Attr Value 1")
            section("Study") {
                attribute("SectAttr", "Sect Attr 1")
            }
        }

        assertThat(submission.accNo).isEqualTo("ABC-123")
        assertThat(submission.attributes).hasSize(1)
        assertThat(submission.attributes.first()).isEqualTo(Attribute("AttrName", "Attr Value 1"))
        assertThat(submission.section).isEqualTo(
            Section("Study", attributes = listOf(Attribute("SectAttr", "Sect Attr 1"))))
    }

    @Test
    fun `section with detailed attributes`() {
        val section = section("Study") {
            accNo = "SECT-001"
            attribute(
                name = "SectAttr",
                value = "Sect Attr 1",
                nameAttrs = mutableListOf(AttributeDetail("NameAttr", "Name Attr 1")),
                valueAttrs = mutableListOf(AttributeDetail("ValueAttr", "Value Attr 1")))
        }

        assertThat(section).isEqualTo(Section(
            type = "Study",
            accNo = "SECT-001",
            attributes = listOf(Attribute(
                name = "SectAttr",
                value = "Sect Attr 1",
                nameAttrs = mutableListOf(AttributeDetail("NameAttr", "Name Attr 1")),
                valueAttrs = mutableListOf(AttributeDetail("ValueAttr", "Value Attr 1"))))))
    }

    @Test
    fun `section with a sections table`() {
        val section = section("Study") {
            accNo = "SECT-001"
            sectionsTable {
                section("Data") { accNo = "DT-1" }
            }
        }

        assertThat(section).isEqualTo(Section(
            "Study",
            "SECT-001",
            sections = mutableListOf(Either.Right(SectionsTable(listOf(Section("Data", "DT-1")))))))
    }

    @Test
    fun `section with inner subsections`() {
        val section = section("Study") {
            accNo = "SECT-001"
            section("Data") { accNo = "DT-1" }
        }
        val expectedSection = Section(
            "Study",
            "SECT-001",
            sections = mutableListOf(Either.Left(Section("Data", "DT-1", parentAccNo = "SECT-001"))))

        assertThat(section).isEqualTo(expectedSection)
    }

    @Test
    fun `section with a file`() {
        val section = section("Study") { file("File1.txt") }
        assertThat(section).isEqualTo(Section("Study", files = mutableListOf(Either.left(File("File1.txt")))))
    }

    @Test
    fun `section with a files table`() {
        val section = section("Study") {
            filesTable {
                file("File1.txt")
            }
        }

        assertThat(section).isEqualTo(Section(
            "Study", files = mutableListOf(Either.Right(FilesTable(listOf(File("File1.txt")))))))
    }

    @Test
    fun `section with a link`() {
        val section = section("Study") { link("http://somelink.org") }
        assertThat(section).isEqualTo(Section("Study", links = mutableListOf(Either.left(Link("http://somelink.org")))))
    }

    @Test
    fun `section with a links table`() {
        val section = section("Study") {
            linksTable {
                link("http://importantsite.net")
            }
        }

        assertThat(section).isEqualTo(Section(
            "Study", links = mutableListOf(Either.Right(LinksTable(listOf(Link("http://importantsite.net")))))))
    }

    @Test
    fun `single sections table`() {
        val sectionsTable = sectionsTable {
            section("Data") { accNo = "DT-1" }
        }

        assertThat(sectionsTable).isEqualTo(SectionsTable(listOf(Section("Data", "DT-1"))))
    }

    @Test
    fun `single file`() {
        val file = file("File1.txt") {
            size = 4
            attribute("Attr1", "ABC")
        }

        assertThat(file).isEqualTo(File("File1.txt", 4, listOf(Attribute("Attr1", "ABC"))))
    }

    @Test
    fun `single files table`() {
        val filesTable = filesTable {
            file("File1.txt")
        }

        assertThat(filesTable).isEqualTo(FilesTable(listOf(File("File1.txt"))))
    }

    @Test
    fun `single link`() {
        val link = link("http://somelink.org") {
            attribute("Attr1", "ABC")
        }

        assertThat(link).isEqualTo(Link("http://somelink.org", listOf(Attribute("Attr1", "ABC"))))
    }

    @Test
    fun `single links table`() {
        val linksTable = linksTable {
            link("http://importantsite.net")
        }

        assertThat(linksTable).isEqualTo(LinksTable(listOf(Link("http://importantsite.net"))))
    }
}
