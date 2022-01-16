package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.model.Section
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.redundent.kotlin.xml.xml

class SectionXmlDeserializerTest {
    private val testInstance = XmlSerializer.mapper

    @Test
    fun `deserialize section`() {
        val xmlSection = xml("section") {
            attribute("accno", "SECT-123")
            attribute("type", "Study")
        }.toString()


        val result = testInstance.readValue(xmlSection, Section::class.java)
        assertThat(result).isEqualTo(Section("Study", "SECT-123"))
    }

    @Test
    fun `deserialize generic section`() {
        val xmlSection = xml("section") {
            attribute("accno", "CMP-123")
            attribute("type", "Compound")
        }.toString()

        assertThat(testInstance.readValue(xmlSection, Section::class.java)).isEqualTo(Section("Compound", "CMP-123"))
    }
}
