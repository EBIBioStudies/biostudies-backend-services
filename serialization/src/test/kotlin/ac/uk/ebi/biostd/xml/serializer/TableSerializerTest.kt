package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.submission.FilesTable
import ac.uk.ebi.biostd.submission.LinksTable
import ac.uk.ebi.biostd.submission.SectionsTable
import ac.uk.ebi.biostd.submission.Table
import ac.uk.ebi.biostd.test.FILE_NAME
import ac.uk.ebi.biostd.test.FILE_SIZE
import ac.uk.ebi.biostd.test.FILE_TYPE
import ac.uk.ebi.biostd.test.LINK_URL
import ac.uk.ebi.biostd.test.SEC_ACC_NO
import ac.uk.ebi.biostd.test.SEC_TYPE
import ac.uk.ebi.biostd.test.simpleFile
import ac.uk.ebi.biostd.test.simpleLink
import ac.uk.ebi.biostd.test.simpleSection
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.junit.jupiter.api.Test
import org.redundent.kotlin.xml.xml
import org.xmlunit.assertj.XmlAssert.assertThat

class TableSerializerTest {

    private val xmlMapper = XmlMapper(JacksonXmlModule().apply { addSerializer(Table::class.java, TableSerializer()) })

    @Test
    fun `Serialize when table is a links table`() {
        val result = xmlMapper.writeValueAsString(LinksTable(listOf(simpleLink())))
        val expected = xml("table") {
            "link"  {
                "url" { -LINK_URL }
                "attributes" {}
            }
        }.toString()

        assertThat(result).and(expected).ignoreWhitespace().areIdentical()
    }

    @Test
    fun `Serialize when table is a file table`() {
        val result = xmlMapper.writeValueAsString(FilesTable(listOf(simpleFile())))
        val expected = xml("table") {
            "file"  {
                "name" { -FILE_NAME }
                "size" { -FILE_SIZE.toString() }
                "type" { -FILE_TYPE }
                "attributes" {}
            }
        }.toString()

        assertThat(result).and(expected).ignoreWhitespace().areIdentical()
    }

    @Test
    fun `Serialize when table is a section table`() {
        val result = xmlMapper.writeValueAsString(SectionsTable(listOf(simpleSection())))
        val expected = xml("table") {
            "section"  {
                "type" { -SEC_TYPE }
                "accNo" { -SEC_ACC_NO }
                "attributes" {}
                "subsections" {}
                "links" {}
                "files" {}
            }
        }.toString()

        assertThat(result).and(expected).ignoreWhitespace().areIdentical()
    }
}