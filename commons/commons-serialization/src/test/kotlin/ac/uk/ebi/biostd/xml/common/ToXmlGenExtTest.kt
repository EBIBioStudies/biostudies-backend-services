package ac.uk.ebi.biostd.xml.common

import com.fasterxml.jackson.dataformat.xml.XmlFactory
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.StringWriter

internal class ToXmlGenExtTest {

    private val xmlFactory = XmlFactory()

    private lateinit var target: StringWriter
    private lateinit var xmlGenerator: ToXmlGenerator

    @BeforeEach
    fun beforeEach() {
        target = StringWriter()
        xmlGenerator = xmlFactory.createGenerator(target)
    }

    @Test
    fun `writeObj with simple properties`() {
        xmlGenerator.run {
            writeXmlObj("example-object") {
                writeXmlField("prop1", "a")
                writeXmlField("prop2", "b")
            }

            xmlGenerator.close()
        }

        assertThat(target.toString())
            .isEqualTo("<example-object><prop1>a</prop1><prop2>b</prop2></example-object>")
    }
}
