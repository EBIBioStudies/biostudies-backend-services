package ac.uk.ebi.biostd.xml

import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import org.junit.Test
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xmlunit.assertj.XmlAssert.assertThat
import java.io.StringReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class XmlSerializerITest {

    private val testInstance = XmlSerializer()

    @Test
    fun serialize() {
        val sub = testInstance.serialize(createVenousBloodMonocyte())
        val xmlDocument = asXmlDocument(sub)

        assertThat(xmlDocument).valueByXPath("//submission/@accNo").isEqualTo("S-IHECRE00000919.1")
    }

    private fun asXmlDocument(doc: String): Document {
        val factory = DocumentBuilderFactory.newInstance()
        val builder: DocumentBuilder

        builder = factory.newDocumentBuilder()
        return builder.parse(InputSource(StringReader(doc)))
    }

}
