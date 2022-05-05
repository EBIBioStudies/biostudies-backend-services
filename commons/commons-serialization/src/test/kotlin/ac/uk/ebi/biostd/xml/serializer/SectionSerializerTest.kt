package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.createFileList
import ac.uk.ebi.biostd.test.FILE_NAME
import ac.uk.ebi.biostd.test.FILE_SIZE
import ac.uk.ebi.biostd.test.FILE_TYPE
import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.link
import ebi.ac.uk.dsl.section
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.constants.SectionFields.FILE_LIST
import org.junit.jupiter.api.Test
import org.redundent.kotlin.xml.xml
import org.xmlunit.assertj.XmlAssert.assertThat

private const val LINK_URL = "link_url"
private const val ACC_NO = "acc no"
private const val TYPE = "sec type"
private const val FILE_LIST_NAME = "file-list"

class SectionSerializerTest {
    private val testInstance = XmlSerializer.mapper
    private val section = section(TYPE) {
        accNo = ACC_NO
        fileList = FileList(FILE_LIST_NAME, createFileList())
        link(LINK_URL)
        file(FILE_NAME) {
            type = FILE_TYPE
            size = FILE_SIZE
        }
    }

    @Test
    fun testSerializeSection() {
        val result = testInstance.writeValueAsString(section)
        val expected = xml("section") {
            attribute("accno", ACC_NO)
            attribute("type", TYPE)
            "attributes" {
                "attribute" {
                    "name" { -FILE_LIST.value }
                    "value" { -"$FILE_LIST_NAME.xml" }
                }
            }
            "links" {
                "link" {
                    "url" { -LINK_URL }
                }
            }
            "files" {
                "file" {
                    attribute("size", FILE_SIZE)
                    "path" { -FILE_NAME }
                    "type" { -FILE_TYPE }
                }
            }
        }.toString()

        assertThat(result).and(expected).ignoreChildNodesOrder().ignoreWhitespace().areIdentical()
    }
}
