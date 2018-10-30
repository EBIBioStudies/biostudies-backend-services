package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.test.FILE_NAME
import ac.uk.ebi.biostd.test.FILE_SIZE
import ac.uk.ebi.biostd.test.FILE_TYPE
import ac.uk.ebi.biostd.test.simpleFile
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import ebi.ac.uk.model.File
import org.junit.jupiter.api.Test
import org.redundent.kotlin.xml.xml
import org.xmlunit.assertj.XmlAssert.assertThat

class FileSerializerTest {

    private val xmlMapper = XmlMapper(JacksonXmlModule().apply { addSerializer(File::class.java, FileSerializer()) })

    private val file = simpleFile()

    @Test
    fun serializeXml() {
        val result = xmlMapper.writeValueAsString(file)
        val expected = xml("file") {
            "name" { -FILE_NAME }
            "type" { -FILE_TYPE }
            "size" { -FILE_SIZE.toString() }
        }.toString()

        assertThat(result).and(expected).ignoreWhitespace().areIdentical()
    }
}
