package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.tsv.deserialization.TsvDeserializer
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.filesTable
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.link
import ebi.ac.uk.dsl.linksTable
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TsvSingleElementDeserializationTest {
    private val deserializer = TsvDeserializer()

    @Test
    fun `single file`() {
        val tsv = tsv {
            line("File", "File1.txt")
            line("Attr", "Value")
            line()
        }.toString()

        assertThat(deserializer.deserializeElement<File>(tsv)).isEqualTo(
            file("File1.txt") {
                attribute("Attr", "Value")
            })
    }

    @Test
    fun `single files table`() {
        val tsv = tsv {
            line("Files", "Attr")
            line("File1.txt", "Value")
            line()
        }.toString()

        assertThat(deserializer.deserializeElement<FilesTable>(tsv)).isEqualTo(
            filesTable {
                file("File1.txt") {
                    attribute("Attr", "Value")
                }
            })
    }

    @Test
    fun `single link`() {
        val tsv = tsv {
            line("Link", "http://alink.org")
            line("Attr", "Value")
            line()
        }.toString()

        assertThat(deserializer.deserializeElement<Link>(tsv)).isEqualTo(
            link("http://alink.org") {
                attribute("Attr", "Value")
            })
    }

    @Test
    fun `single links table`() {
        val tsv = tsv {
            line("Links", "Attr")
            line("http://alink.org", "Value")
            line()
        }.toString()

        assertThat(deserializer.deserializeElement<LinksTable>(tsv)).isEqualTo(
            linksTable {
                link("http://alink.org") {
                    attribute("Attr", "Value")
                }
            })
    }

    @Test
    fun `table containing an empty attribute`() {
        val tsv = tsv {
            line("Links", "Attr1", "Attr2")
            line("AF069307", "Value 1", "Value 2")
            line("AF069308", "", "Value 2")
            line("AF069309", "Value 1")
            line()
        }.toString()

        assertThat(deserializer.deserializeElement<LinksTable>(tsv)).isEqualTo(
            linksTable {
                link("AF069307") {
                    attribute("Attr1", "Value 1")
                    attribute("Attr2", "Value 2")
                }

                link("AF069308") {
                    attribute("Attr1", "")
                    attribute("Attr2", "Value 2")
                }

                link("AF069309") {
                    attribute("Attr1", "Value 1")
                }
            })
    }
}
