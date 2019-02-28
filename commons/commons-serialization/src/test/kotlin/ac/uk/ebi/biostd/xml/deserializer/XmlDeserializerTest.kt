package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.xml.XmlSerializer
import ac.uk.ebi.biostd.xml.deserializer.exception.InvalidXmlPageTabElementException
import ac.uk.ebi.biostd.xml.deserializer.exception.UnexpectedXmlPageTabElementException
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.extensions.title
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.redundent.kotlin.xml.xml

class XmlDeserializerTest {
    private val testInstance = XmlSerializer()

    @Test
    fun `deserialize submission`() {
        val xml = xml("submission") {
            attribute("accNo", "ABC123")
            "attributes" {
                "attribute" {
                    "name" { -"Title" }
                    "value" { -"Test Submission" }
                }
            }
            "section" {
                attribute("accNo", "SECT-123")
                attribute("type", "Study")
            }
        }.toString()

        val expected = submission("ABC123") {
            title = "Test Submission"

            section("Study") {
                accNo = "SECT-123"
            }
        }

        assertThat(testInstance.deserialize(xml)).isEqualTo(expected)
    }

    @Test
    fun `deserialize section`() {
        val xml = xml("section") {
            attribute("accNo", "SECT-123")
            attribute("type", "Study")
        }.toString()

        assertThat(testInstance.deserialize(xml, Section::class.java)).isEqualTo(Section("Study", "SECT-123"))
    }

    @Test
    fun `deserialize file`() {
        val xml = xml("file") {
            "path" { -"file1.txt" }
        }.toString()

        assertThat(testInstance.deserialize(xml, File::class.java)).isEqualTo(File("file1.txt"))
    }

    @Test
    fun `deserialize files table`() {
        val expected = FilesTable(listOf(File("file1.txt")))
        val xml = xml("table") {
            "file" {
                "path" { -"file1.txt" }
            }
        }.toString()

        assertThat(testInstance.deserialize(xml, FilesTable::class.java)).isEqualTo(expected)
    }

    @Test
    fun `deserialize link`() {
        val xml = xml("link") {
            "url" { -"http://arandomurl.org" }
        }.toString()

        assertThat(testInstance.deserialize(xml, Link::class.java)).isEqualTo(Link("http://arandomurl.org"))
    }

    @Test
    fun `deserialize links table`() {
        val expected = LinksTable(listOf(Link("http://arandomurl.org")))
        val xml = xml("table") {
            "link" {
                "url" { -"http://arandomurl.org" }
            }
        }.toString()

        assertThat(testInstance.deserialize(xml, LinksTable::class.java)).isEqualTo(expected)
    }

    @Test
    fun `deserialize invalid element`() {
        val xml = xml("attribute") {
            "name" { -"Title" }
            "value" { -"Test Submission" }
        }.toString()

        assertThrows<InvalidXmlPageTabElementException> { testInstance.deserialize(xml, Attribute::class.java) }
    }

    @Test
    fun `deserialize invalid table`() {
        val xml = xml("file") {
            "path" { -"file1.txt" }
        }.toString()

        assertThrows<UnexpectedXmlPageTabElementException> { testInstance.deserialize(xml, FilesTable::class.java) }
    }}
