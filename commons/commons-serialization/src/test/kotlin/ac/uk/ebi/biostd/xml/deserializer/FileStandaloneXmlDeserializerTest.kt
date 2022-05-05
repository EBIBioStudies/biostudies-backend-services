package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_FILE_PATH
import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

        assertThat(testInstance.readValue(xmlFile, BioFile::class.java)).isEqualTo(
            BioFile("file1.txt", attributes = mutableListOf(Attribute("attr1", "attr 1 value")))
        )
    }

    @Test
    fun `deserialize file without attributes`() {
        val xmlFile = xml("file") {
            "path" { -"file1.txt" }
        }.toString()

        assertThat(testInstance.readValue(xmlFile, BioFile::class.java)).isEqualTo(BioFile("file1.txt"))
    }

    @Test
    fun `deserialize file without path`() {
        val xmlFile = xml("file") {
            "path" { -"" }
        }.toString()

        val exception = assertThrows<InvalidElementException> { testInstance.readValue(xmlFile, BioFile::class.java) }
        assertThat(exception.message).isEqualTo("$REQUIRED_FILE_PATH. Element was not created.")
    }
}
