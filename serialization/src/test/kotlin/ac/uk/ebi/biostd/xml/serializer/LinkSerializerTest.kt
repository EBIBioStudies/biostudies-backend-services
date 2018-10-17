package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.test.LINK_URL
import ac.uk.ebi.biostd.test.simpleLink
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.junit.jupiter.api.Test
import org.redundent.kotlin.xml.xml
import org.xmlunit.assertj.XmlAssert.assertThat

class LinkSerializerTest {

    private val xmlMapper = XmlMapper(JacksonXmlModule().apply { addSerializer(Link::class.java, LinkSerializer()) })

    private val link = simpleLink()

    @Test
    fun serializeXml() {
        val result = xmlMapper.writeValueAsString(link)
        val expected = xml("link") {
            "url" { -LINK_URL }
        }.toString()

        assertThat(result).and(expected).ignoreWhitespace().areIdentical()
    }
}
