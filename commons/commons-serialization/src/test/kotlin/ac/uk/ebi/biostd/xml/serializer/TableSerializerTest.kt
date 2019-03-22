package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.common.EitherSerializer
import ac.uk.ebi.biostd.test.FILE_NAME
import ac.uk.ebi.biostd.test.LINK_URL
import ac.uk.ebi.biostd.test.SEC_ACC_NO
import ac.uk.ebi.biostd.test.SEC_TYPE
import ac.uk.ebi.biostd.test.simpleFile
import ac.uk.ebi.biostd.test.simpleLink
import ac.uk.ebi.biostd.test.simpleSection
import arrow.core.Either
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.sectionsTable
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Table
import org.junit.jupiter.api.Test
import org.redundent.kotlin.xml.xml
import org.xmlunit.assertj.XmlAssert.assertThat

class TableSerializerTest {

    private val xmlMapper = XmlMapper(JacksonXmlModule().apply {
        addSerializer(File::class.java, FileSerializer())
        addSerializer(Link::class.java, LinkSerializer())
        addSerializer(Table::class.java, TableSerializer())
        addSerializer(Either::class.java, EitherSerializer())
        addSerializer(Section::class.java, SectionSerializer())
        addSerializer(Attribute::class.java, AttributeSerializer())
    })

    @Test
    fun `Serialize when table is a links table`() {
        val result = xmlMapper.writeValueAsString(LinksTable(listOf(simpleLink())))
        val expected = xml("table") {
            "link" {
                "url" { -LINK_URL }
            }
        }.toString()

        assertThat(result).and(expected).ignoreWhitespace().areIdentical()
    }

    @Test
    fun `Serialize when table is a file table`() {
        val result = xmlMapper.writeValueAsString(FilesTable(listOf(simpleFile())))
        val expected = xml("table") {
            "file" {
                attribute("size", "0")
                "path" { -FILE_NAME }
            }
        }.toString()

        assertThat(result).and(expected).ignoreWhitespace().areIdentical()
    }

    @Test
    fun `Serialize when table is a section table`() {
        val result = xmlMapper.writeValueAsString(SectionsTable(listOf(simpleSection())))
        val expected = xml("table") {
            "section" {
                attribute("type", SEC_TYPE)
                attribute("accno", SEC_ACC_NO)
            }
        }.toString()

        assertThat(result).and(expected).ignoreWhitespace().areIdentical()
    }

    @Test
    fun `serialize sections table with attributes`() {
        val section = section("Study") {
            accNo = "SECT-001"
            sectionsTable {
                section("Data") {
                    accNo = "DT-1"
                    parentAccNo = "SECT-001"
                    attribute(
                        name = "Attr",
                        value = "The Value",
                        nameAttrs = mutableListOf(AttributeDetail("NameAttr", "A")),
                        valueAttrs = mutableListOf(AttributeDetail("ValAttr", "B")))
                }
            }
        }

        val expected = xml("section") {
            attribute("type", "Study")
            attribute("accno", "SECT-001")
            "subsections" {
                "table" {
                    "section" {
                        attribute("type", "Data")
                        attribute("accno", "DT-1")
                        "attributes" {
                            "attribute" {
                                "name" { -"Attr" }
                                "value" { -"The Value" }
                                "nmqual" {
                                    "name" { -"NameAttr" }
                                    "value" { -"A" }
                                }
                                "valqual" {
                                    "name" { -"ValAttr" }
                                    "value" { -"B" }
                                }
                            }
                        }
                    }
                }
            }
        }.toString()

        assertThat(xmlMapper.writeValueAsString(section)).and(expected).ignoreWhitespace().areIdentical()
    }
}
