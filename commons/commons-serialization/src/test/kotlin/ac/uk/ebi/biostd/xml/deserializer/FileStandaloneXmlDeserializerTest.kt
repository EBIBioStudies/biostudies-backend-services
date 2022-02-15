package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.redundent.kotlin.xml.xml

class FileStandaloneXmlDeserializerTest {
    private val testInstance = XmlSerializer.mapper

    @Test
    fun `deserialize file`() {
        val xmlFile = xml("file") {
            "path" { -"file1.txt" }
            "attributes" {
                "attribute" {
                    "name" { -"attr1" }
                    "value" { -"attr 1 value" }
                }
            }
        }.toString()

        assertThat(testInstance.readValue(xmlFile, File::class.java)).isEqualTo(
            File("file1.txt", attributes = mutableListOf(Attribute("attr1", "attr 1 value")))
        )
    }

    @Test
    fun `deserialize file without attributes`() {
        val xmlFile = xml("file") {
            "path" { -"file1.txt" }
        }.toString()

        assertThat(testInstance.readValue(xmlFile, File::class.java)).isEqualTo(File("file1.txt"))
    }
}
