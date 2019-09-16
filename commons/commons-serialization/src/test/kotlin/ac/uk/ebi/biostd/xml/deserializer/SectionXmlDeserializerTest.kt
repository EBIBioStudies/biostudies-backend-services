package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.xml.common.createXmlDocument
import ac.uk.ebi.biostd.xml.deserializer.TestDeserializerFactory.Companion.sectionXmlDeserializer
import ebi.ac.uk.model.Section
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.redundent.kotlin.xml.xml

class SectionXmlDeserializerTest {
    private val testInstance = sectionXmlDeserializer()

    @Test
    fun `deserialize section`() {
        val xmlSection = createXmlDocument(
            xml("section") {
                attribute("accno", "SECT-123")
                attribute("type", "Study")
            }.toString())

        assertThat(testInstance.deserialize(xmlSection)).isEqualTo(Section("Study", "SECT-123"))
    }

    @Test
    fun `deserialize generic section`() {
        val xmlSection = createXmlDocument(
            xml("section") {
                attribute("accno", "CMP-123")
                attribute("type", "Compound")
            }.toString())

        assertThat(testInstance.deserialize(xmlSection)).isEqualTo(Section("Compound", "CMP-123"))
    }
}
