package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.test.FILE_NAME
import ac.uk.ebi.biostd.test.simpleFile
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import ebi.ac.uk.model.BioFile
import org.junit.jupiter.api.Test
import org.redundent.kotlin.xml.xml
import org.xmlunit.assertj.XmlAssert.assertThat

class FileSerializerTest {
    private val file = simpleFile()
    private val xmlMapper = XmlMapper(JacksonXmlModule().apply { addSerializer(BioFile::class.java, FileSerializer()) })

    @Test
    fun serializeXml() {
        val result = xmlMapper.writeValueAsString(file)
        val expected = xml("file") {
            attribute("size", "0")
            "path" { -FILE_NAME }
            "type" { -"file" }
        }.toString()

        assertThat(result).and(expected).ignoreWhitespace().areIdentical()
    }
}
