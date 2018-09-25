package ac.uk.ebi.biostd.serialization.xml.serializer

import ac.uk.ebi.biostd.serialization.xml.XmlSerializer
import ac.uk.ebi.biostd.submission.file
import ac.uk.ebi.biostd.submission.link
import ac.uk.ebi.biostd.submission.section
import org.junit.Test
import org.redundent.kotlin.xml.xml
import org.xmlunit.assertj.XmlAssert.assertThat

private const val LINK_URL = "link_url"
private const val ACC_NO = "acc no"
private const val TYPE = "sec type"

private const val FILE_NAME = "file_name"
private const val FILE_TYPE = "type"
private const val FILE_SIZE = 150

class SectionSerializerTest {

    private val testInstance = XmlSerializer()

    private val section = section {
        accNo = ACC_NO
        type = TYPE
        link { url = LINK_URL }
        file {
            name = FILE_NAME
            type = FILE_TYPE
            size = FILE_SIZE
        }
    }

    @Test
    fun testSerializeSection() {
        val result = testInstance.serialize(section)
        val expected = xml("section") {
            attribute("accNo", ACC_NO)
            attribute("type", TYPE)
            "links" {
                "link" {
                    "url" { -LINK_URL }
                }
            }
            "files" {
                "file" {
                    "name" { -FILE_NAME }
                    "size" { -FILE_SIZE.toString() }
                    "type" { -FILE_TYPE }
                }
            }
        }.toString()

        assertThat(result).and(expected).ignoreChildNodesOrder().ignoreWhitespace().areIdentical()
    }
}