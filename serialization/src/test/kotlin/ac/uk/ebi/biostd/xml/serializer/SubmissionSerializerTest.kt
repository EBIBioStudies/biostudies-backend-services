package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.model.Submission
import org.junit.Test
import org.redundent.kotlin.xml.xml
import org.xmlunit.assertj.XmlAssert.assertThat

private const val ACC_NO = "ABC-123"
private const val TAG = "Access_Tag"

private const val ATTR_NAME = "attribute_name"
private const val ATTR_VALUE = "attribute_value"

private const val SEC_TYPE = "Study"
private const val SEC_ACC_NO = "SEC-123"

class SubmissionSerializerTest {

    private val testInstance = XmlSerializer()

    private val testSubmission: Submission = submission(ACC_NO) {
        accessTags = mutableListOf(TAG)

        attribute(ATTR_NAME, ATTR_VALUE)

        section(SEC_TYPE) {
            accNo = SEC_ACC_NO
        }
    }

    @Test
    fun testSerializeSubmission() {
        val result = testInstance.serialize(testSubmission)
        val expected = xml("submission") {
            attribute("accNo", ACC_NO)
            "attributes" {
                "attribute" {
                    "name" { -ATTR_NAME }
                    "value" { -ATTR_VALUE }
                }
            }
            "section" {
                attribute("accNo", SEC_ACC_NO)
                attribute("type", SEC_TYPE)
            }
        }.toString()

        assertThat(result).and(expected).ignoreElementContentWhitespace().ignoreChildNodesOrder().ignoreWhitespace().areIdentical()
    }
}
