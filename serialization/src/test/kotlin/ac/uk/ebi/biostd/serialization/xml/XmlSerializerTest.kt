package ac.uk.ebi.biostd.serialization.xml

import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import org.junit.Test
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xmlunit.assertj.XmlAssert.assertThat
import java.io.StringReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class XmlSerializerTest {

    private val testInstance = XmlSerializer()

    @Test
    fun serialize() {
        val sub = createVenousBloodMonocyte()
        val xmlDocument = testInstance.serialize(sub).asXmlDocument()
        assertThat(xmlDocument).valueByXPath("//submission/@acc").isEqualTo("S-IHECRE00000919.1")
    }

    private fun String.asXmlDocument(): Document {
        val factory = DocumentBuilderFactory.newInstance()
        val builder: DocumentBuilder

        builder = factory.newDocumentBuilder()
        return builder.parse(InputSource(StringReader(this)))
    }

}
