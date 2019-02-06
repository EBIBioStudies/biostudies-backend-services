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
    fun `create submission with attributes`() {
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
    fun `create section with detailed attributes`() {
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
    fun `create a section with a sections table`() {
        val section = section("Study") {
            accNo = "SECT-001"
            sectionsTable {
                section("Data") { accNo = "DT-1" }
            }
        }

        assertThat(section).isEqualTo(Section(
            "Study", "SECT-001", sections = mutableListOf(Either.Right(SectionsTable(listOf(Section("Data", "DT-1")))))))
    }

    @Test
    fun `create a section with a file`() {
        val section = section("Study") { file("File1.txt") }
        assertThat(section).isEqualTo(Section("Study", files = mutableListOf(Either.left(File("File1.txt")))))
    }

    @Test
    fun `create a section with a files table`() {
        val section = section("Study") {
            filesTable {
                file("File1.txt")
            }
        }

        assertThat(section).isEqualTo(Section(
            "Study", files = mutableListOf(Either.Right(FilesTable(listOf(File("File1.txt")))))))
    }

    @Test
    fun `create a section with a link`() {
        val section = section("Study") { link("http://somelink.org") }
        assertThat(section).isEqualTo(Section("Study", links = mutableListOf(Either.left(Link("http://somelink.org")))))
    }

    @Test
    fun `create a section with a links table`() {
        val section = section("Study") {
            linksTable {
                link("http://importantsite.net")
            }
        }

        assertThat(section).isEqualTo(Section(
            "Study", links = mutableListOf(Either.Right(LinksTable(listOf(Link("http://somelink.org")))))))
    }
}
