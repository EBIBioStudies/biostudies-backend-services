package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.tsv.serialization.TsvToStringSerializer
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.sectionsTable
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TsvToStringSerializerTest {
    private val testInstance = TsvToStringSerializer()

    @Test
    fun `serialize submission`() {
        val submission = submission("ABC-123") {
            attribute("Title", "Test Submission")
        }

        val expectedTsv = tsv {
            line("Submission", "ABC-123")
            line("Title", "Test Submission")
            line()
        }.toString()

        assertThat(testInstance.serialize(submission)).isEqualToIgnoringNewLines(expectedTsv)
    }

    @Test
    fun `serialize section`() {
        val section = section("Study") {
            fileList = FileList("file-list")
            attribute("Project", "Test Project")
        }

        val expectedTsv = tsv {
            line("Study")
            line("Project", "Test Project")
            line("File List", "file-list.pagetab.tsv")
            line()
        }.toString()

        assertThat(testInstance.serialize(section)).isEqualToIgnoringNewLines(expectedTsv)
    }

    @Test
    fun `serialize section with attribute details`() {
        val section = section("Sample") {
            attribute(
                name = "Tissue Type",
                value = "venous blood",
                nameAttrs = mutableListOf(AttributeDetail("Tissue", "Blood")),
                valueAttrs = mutableListOf(AttributeDetail("Ontology", "UBERON"))
            )
        }

        val expectedTsv = tsv {
            line("Sample")
            line("Tissue Type", "venous blood")
            line("(Tissue)", "Blood")
            line("[Ontology]", "UBERON")
            line()
        }.toString()

        assertThat(testInstance.serialize(section)).isEqualToIgnoringNewLines(expectedTsv)
    }

    @Test
    fun `serialize subsection`() {
        val section = section("Study") {
            accNo = "SECT-001"
            attribute("Project", "Test Project")

            section("Data") {
                accNo = "DT-1"
                attribute("Type", "data")
            }
        }

        val expectedTsv = tsv {
            line("Study", "SECT-001")
            line("Project", "Test Project")
            line()

            line("Data", "DT-1", "SECT-001")
            line("Type", "data")
            line()
        }.toString()

        assertThat(testInstance.serialize(section)).isEqualToIgnoringNewLines(expectedTsv)
    }

    @Test
    fun `serialize subsection without accession and parent accession`() {
        val section = section("Study") {
            accNo = "SECT-001"
            attribute("Project", "Test Project")

            section("Author") {
                attribute("Name", "The Author")
            }
        }

        val expectedTsv = tsv {
            line("Study", "SECT-001")
            line("Project", "Test Project")
            line()

            line("Author", "", "SECT-001")
            line("Name", "The Author")
            line()
        }.toString()

        assertThat(testInstance.serialize(section)).isEqualToIgnoringNewLines(expectedTsv)
    }

    @Test
    fun `serialize subsection with accession and no parent accession`() {
        val section = section("Study") {
            attribute("Project", "Test Project")

            section("Author") {
                attribute("Name", "The Author")
            }
        }

        val expectedTsv = tsv {
            line("Study")
            line("Project", "Test Project")
            line()

            line("Author")
            line("Name", "The Author")
            line()
        }.toString()

        assertThat(testInstance.serialize(section)).isEqualToIgnoringNewLines(expectedTsv)
    }

    @Test
    fun `serialize subsections table`() {
        val section = section("Study") {
            accNo = "SECT-001"
            attribute("Project", "Test Project")
            sectionsTable {
                section("Data") {
                    accNo = "DT-1"
                    parentAccNo = "SECT-001"
                    attribute("Type", "data")
                }
            }
        }

        val expectedTsv = tsv {
            line("Study", "SECT-001")
            line("Project", "Test Project")
            line()

            line("Data[SECT-001]", "Type")
            line("DT-1", "data")
        }.toString()

        assertThat(testInstance.serialize(section)).isEqualToIgnoringNewLines(expectedTsv)
    }

    @Test
    fun `serialize file`() {
        val file = File("File1.txt")
        val expectedTsv = tsv {
            line("File", "File1.txt")
        }.toString()

        assertThat(testInstance.serialize(file)).isEqualToIgnoringNewLines(expectedTsv)
    }

    @Test
    fun `serialize link`() {
        val link = Link("http://importantsite.org")
        val expectedTsv = tsv {
            line("Link", "http://importantsite.org")
        }.toString()

        assertThat(testInstance.serialize(link)).isEqualToIgnoringNewLines(expectedTsv)
    }

    @Test
    fun `serialize files table`() {
        val filesTable = FilesTable(listOf(File("File1.txt", attributes = listOf(Attribute("Type", "Text")))))
        val expectedTsv = tsv {
            line("Files", "Type")
            line("File1.txt", "Text")
        }.toString()

        assertThat(testInstance.serialize(filesTable)).isEqualToIgnoringNewLines(expectedTsv)
    }

    @Test
    fun `serialize files table with detailed attributes`() {
        val filesTable = FilesTable(
            listOf(
                File(
                    "File1.txt",
                    attributes = listOf(
                        Attribute(
                            name = "Type",
                            value = "Text",
                            nameAttrs = mutableListOf(AttributeDetail("Name Attr", "a")),
                            valueAttrs = mutableListOf(AttributeDetail("Val Attr", "b"))
                        )
                    )
                )
            )
        )

        val expectedTsv = tsv {
            line("Files", "Type", "(Name Attr)", "[Val Attr]")
            line("File1.txt", "Text", "a", "b")
        }.toString()

        assertThat(testInstance.serialize(filesTable)).isEqualToIgnoringNewLines(expectedTsv)
    }

    @Test
    fun `serialize links table`() {
        val linksTable = LinksTable(listOf(Link("FGH765", attributes = listOf(Attribute("Type", "Gen")))))
        val expectedTsv = tsv {
            line("Links", "Type")
            line("FGH765", "Gen")
        }.toString()

        assertThat(testInstance.serialize(linksTable)).isEqualToIgnoringNewLines(expectedTsv)
    }

    @Test
    fun `serialize sections table`() {
        val sectionsTable =
            SectionsTable(listOf(Section("Data", "DT-1", attributes = listOf(Attribute("Type", "data")))))
        val expectedTsv = tsv {
            line("Data[]", "Type")
            line("DT-1", "data")
        }.toString()

        assertThat(testInstance.serialize(sectionsTable)).isEqualToIgnoringNewLines(expectedTsv)
    }
}
