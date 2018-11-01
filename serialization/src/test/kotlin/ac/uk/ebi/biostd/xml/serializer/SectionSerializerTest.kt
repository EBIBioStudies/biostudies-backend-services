package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.test.FILE_NAME
import ac.uk.ebi.biostd.test.FILE_SIZE
import ac.uk.ebi.biostd.test.FILE_TYPE
import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.link
import ebi.ac.uk.dsl.section
import ebi.ac.uk.model.extensions.size
import ebi.ac.uk.model.extensions.type
import org.junit.Test
import org.redundent.kotlin.xml.xml
import org.xmlunit.assertj.XmlAssert.assertThat

private const val LINK_URL = "link_url"
private const val ACC_NO = "acc no"
private const val TYPE = "sec type"

class SectionSerializerTest {

    private val testInstance = XmlSerializer()

    private val section = section {
        accNo = ACC_NO
        type = TYPE
        link(LINK_URL)
        file(FILE_NAME) {
            type = FILE_TYPE
            size = FILE_SIZE
        }
    }

    @Test
    fun testSerializeSection() {
        val result = testInstance.serialize(section)
        val expected = xml("section") {
            attribute("accNo", ACC_NO)
            "attributes" {
                "attribute" {
                    "name" { -"type" }
                    "value" { -TYPE }
                }
            }
            "links" {
                "link" {
                    "url" { -LINK_URL }
                }
            }
            "files" {
                "file" {
                    "name" { -FILE_NAME }
                    "attributes" {
                        "attribute" {
                            "name" { -"type" }
                            "value" { -FILE_TYPE }
                        }
                        "attribute" {
                            "name" { -"size" }
                            "value" { -FILE_SIZE.toString() }
                        }
                    }
                }
            }
        }.toString()

        assertThat(result).and(expected).ignoreChildNodesOrder().ignoreWhitespace().areIdentical()
    }
}