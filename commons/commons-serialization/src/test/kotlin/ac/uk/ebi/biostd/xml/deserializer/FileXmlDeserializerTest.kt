package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.xml.common.createXmlDocument
import ac.uk.ebi.biostd.xml.deserializer.TestDeserializerFactory.Companion.fileXmlDeserializer
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.redundent.kotlin.xml.xml

class FileXmlDeserializerTest {
    private val testInstance = fileXmlDeserializer()

    @Test
    fun `deserialize file`() {
        val xmlFile = createXmlDocument(
            xml("file") {
                "path" { -"file1.txt" }
                "attributes" {
                    "attribute" {
                        "name" { -"attr1" }
                        "value" { -"attr 1 value" }
                    }
                }
            }.toString()
        )

        assertThat(testInstance.deserialize(xmlFile)).isEqualTo(
            File("file1.txt", attributes = mutableListOf(Attribute("attr1", "attr 1 value")))
        )
    }

    @Test
    fun `deserialize file without attributes`() {
        val xmlFile = createXmlDocument(
            xml("file") {
                "path" { -"file1.txt" }
            }.toString()
        )

        assertThat(testInstance.deserialize(xmlFile)).isEqualTo(File("file1.txt"))
    }
}
